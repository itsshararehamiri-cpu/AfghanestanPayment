package com.danesh.engine

import android.util.Log
import com.danesh.api.TransactionContextProvider
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
        val isInit = handler::class.simpleName == "InitHandler"
        if (isInit) Log.i("BpInit", "Executor | شروع تراکنش ISO")
        Log.i(TAG, "building message for ${handler::class.simpleName}")
        val message = handler.buildMessage(request)
        var flushAdviceAfterClose = false
        val result = try {
            connection.use { conn ->
                // قبل از ارسال 0200: ثبت SAF با status=R برای تراکنش‌های قابل‌برگشت
                if (handler.isReversible) {
                    store.registerSaf(message)
                }

                try {
                    if (isInit) Log.i("BpInit", "Executor | اتصال به هاست")
                    conn.connect()
                } catch (e: Exception) {
                    Log.e(TAG, "connect failed: ${e.message}", e)
                    if (isInit) Log.e("BpInit", "Executor | اتصال ناموفق", e)
                    // 0200 ارسال نشده → نیازی به Reverse نیست
                    discardSaf(handler, message)
                    return@use handler.connectFailure(request, message, e)
                }

                try {
                    if (isInit) Log.i("BpInit", "Executor | ارسال پیام")
                    message.printEachField("req>>")
                    conn.send(message)
                } catch (e: Exception) {
                    Log.e(TAG, "send failed: ${e.message}", e)
                    if (isInit) Log.e("BpInit", "Executor | ارسال ناموفق", e)
                    // status همان R می‌ماند → بعداً Reverse (0400)
                    return@use handler.sendFailure(request, message, e)
                }
                val response = try {
                    if (isInit) Log.i("BpInit", "Executor | دریافت پاسخ")
                    conn.receive()
                } catch (e: Exception) {
                    Log.e(TAG, "receive failed: ${e.message}", e.cause)
                    Log.e(TAG, "receive failed: ${e.message}${e.message.toString()}", )

                    if (isInit) Log.e("BpInit", "Executor | دریافت ناموفق", e)
                    // Timeout / قطع ارتباط → status=R → Reverse
                    return@use handler.receiveFailure(request, message, e)
                }
                response?.printEachField("res>>")
                if (response == null) {
                    return@use handler.receiveFailure(
                        request,
                        message,
                        Exception("No response from host"),
                    )
                }

                hostTimeSynchronizer.syncFromResponse(response)
                if (handler.isFailure(response)) {
                    if (isInit) Log.i("BpInit", "Executor | پاسخ ناموفق")
                    discardSaf(handler, message)
                    handler.failure(request, message, response)
                } else {
                    if (isInit) Log.i("BpInit", "Executor | پاسخ موفق")
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
            // اگر قبلاً register شده و confirm نشده، status=R می‌ماند
            handler.networkError(request, message, e)
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
