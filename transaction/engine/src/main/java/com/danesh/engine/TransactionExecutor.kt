package com.danesh.engine

import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.common.diagnostics.StartupTraceFile
import com.danesh.api.TransactionRequest
import com.danesh.api.TransactionResult
import com.danesh.common.RawMessage
import com.danesh.core.Connection
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TransactionExecutor"

@Singleton
class TransactionExecutor<M : RawMessage> @Inject constructor(
    private val connection: Connection<M>,
    private val store: TransactionStore<M>,
    private val queueProcessor: QueueProcessor<M>,
    private val hostTimeSynchronizer: HostTimeSynchronizer,
    private val contextProvider: TransactionContextProvider,
    private val activeTransactionGuard: ActiveTransactionGuard,
) {

    suspend fun <Req : TransactionRequest, Res : TransactionResult> execute(
        request: Req,
        handler: HandlerTransaction<Req, Res, M>,
        skipQueueFlush: Boolean = handler.skipQueueFlush,
    ): Res {
        activeTransactionGuard.enter()
        try {
            if (!skipQueueFlush && !queueProcessor.checkTxns(force = true)) {
                return handler.queueFailure(request)
            }
            Log.d(TAG, "execute: kkkkkkkkkk")
            return executeTransaction(request, handler)
        } finally {
            activeTransactionGuard.exit()
        }
    }

    private suspend fun <Req : TransactionRequest, Res : TransactionResult> executeTransaction(
        request: Req,
        handler: HandlerTransaction<Req, Res, M>,
    ): Res {
        val handlerName = handler::class.simpleName.orEmpty()
        val isStartup = handlerName == "InitHandler" || handlerName == "LogonHandler"
        val isInit = handlerName == "InitHandler"
        if (isStartup) StartupTraceFile.begin("Executor $handlerName")
        if (isInit) Log.i("BpInit", "Executor | شروع تراکنش ISO")
        Log.i(TAG, "building message for $handlerName")
        if (isStartup) StartupTraceFile.line("Executor", "buildMessage $handlerName")
        val message = try {
            handler.buildMessage(request)
        } catch (error: Exception) {
            if (isStartup) StartupTraceFile.error("Executor buildMessage", error)
            if (isStartup) StartupTraceFile.end("Executor $handlerName")
            throw error
        }
        if (isStartup) StartupTraceFile.line("Executor", "buildMessage done $handlerName")
        var flushAdviceAfterClose = false
        val result = try {
            connection.use { conn ->
                // قبل از ارسال 0200: ثبت SAF با status=R برای تراکنش‌های قابل‌برگشت
                if (handler.isReversible) {
                    store.registerSaf(message)
                }

                try {
                    if (isInit) Log.i("BpInit", "Executor | اتصال به هاست")
                    if (isStartup) StartupTraceFile.line("Executor", "connect")
                    conn.connect()
                    if (isStartup) StartupTraceFile.line("Executor", "connect ok")
                } catch (e: Exception) {
                    Log.e(TAG, "connect failed: ${e.message}", e)
                    if (isInit) Log.e("BpInit", "Executor | اتصال ناموفق", e)
                    if (isStartup) StartupTraceFile.error("Executor connect", e)
                    // 0200 ارسال نشده → نیازی به Reverse نیست
                    discardSaf(handler, message)
                    return@use handler.connectFailure(request, message, e)
                }

                try {
                    if (isInit) Log.i("BpInit", "Executor | ارسال پیام")
                    if (isStartup) StartupTraceFile.line("Executor", "send")
                    message.printEachField("req>>")
                    conn.send(message)
                    if (isStartup) StartupTraceFile.line("Executor", "send ok")
                } catch (e: Exception) {
                    Log.e(TAG, "send failed: ${e.message}", e)
                    if (isInit) Log.e("BpInit", "Executor | ارسال ناموفق", e)
                    if (isStartup) StartupTraceFile.error("Executor send", e)
                    // status همان R می‌ماند → بعداً Reverse (0400)
                    return@use handler.sendFailure(request, message, e)
                }
                val response = try {
                    if (isInit) Log.i("BpInit", "Executor | دریافت پاسخ")
                    if (isStartup) StartupTraceFile.line("Executor", "receive")
                    conn.receive()
                } catch (e: Exception) {
                    Log.e(TAG, "receive failed: ${e.message}", e.cause)
                    Log.e(TAG, "receive failed: ${e.message}${e.message.toString()}", )

                    if (isInit) Log.e("BpInit", "Executor | دریافت ناموفق", e)
                    if (isStartup) StartupTraceFile.error("Executor receive", e)
                    // Timeout / قطع ارتباط → status=R → Reverse
                    return@use handler.receiveFailure(request, message, e)
                }
                response?.printEachField("res>>")
                if (response == null) {
                    if (isStartup) StartupTraceFile.line("Executor", "receive empty")
                    return@use handler.receiveFailure(
                        request,
                        message,
                        Exception("No response from host"),
                    )
                }
                if (isStartup) StartupTraceFile.line("Executor", "receive ok")

                hostTimeSynchronizer.syncFromResponse(response)
                if (handler.isFailure(response)) {
                    if (isInit) Log.i("BpInit", "Executor | پاسخ ناموفق")
                    if (isStartup) StartupTraceFile.line("Executor", "host failure")
                    discardSaf(handler, message)
                    handler.failure(request, message, response)
                } else {
                    if (isInit) Log.i("BpInit", "Executor | پاسخ موفق")
                    if (isStartup) StartupTraceFile.line("Executor", "host success")
                    if (handler.needReport) {
                        store.saveReport(message, response)
                    }
                    if (handler.needAdvice) {
                        // confirmTxn: R → S → Advice (0220) پس از نمایش رسید
                        store.confirmTxn(message, response)
                        flushAdviceAfterClose = !handler.deferAdviceUntilReceipt
                    } else {
                        // تراکنش موفق بدون نیاز به Advice → حذف از SAF
                        discardSaf(handler, message)
                    }
                    if (handler.recordsLastSuccessReference) {
                        contextProvider.saveLastSuccessfulTransaction(
                            stan = message.field11Stan.orEmpty(),
                            rrn = response?.field37Rrn,
                        )
                    }
                    handler.success(request, message, response)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "transaction failed: ${e.message}", e)
            if (isStartup) StartupTraceFile.error("Executor networkError", e)
            // اگر قبلاً register شده و confirm نشده، status=R می‌ماند
            handler.networkError(request, message, e)
        } finally {
            if (isStartup) StartupTraceFile.end("Executor $handlerName")
        }
        if (flushAdviceAfterClose) {
            // بعد از بستن اتصال مالی، Advice (0220) را ارسال کن؛ شکست تراکنش را fail نمی‌کند
            runCatching { queueProcessor.checkTxns(force = true) }
        }
        return result
    }

    private suspend fun discardSaf(
        handler: HandlerTransaction<*, *, M>,
        message: M,
    ) {
        if (handler.isReversible) {
            store.clearSaf(message)
        }
    }
}
