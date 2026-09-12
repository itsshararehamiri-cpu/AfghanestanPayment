package com.danesh.hp.config


import android.util.Log
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.key.HpKeyConfig
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalConfigHandler @Inject constructor(
    private val terminalConfigMessageMessageBuilder: HpTerminalConfigMessageHandler,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
    private val configurationStore: DeviceConfigurationStore,
    private val deviceOperations: PspDeviceOperations,
    private val terminalConfigStore: HpTerminalConfigStore,
    private val contextProvider: TransactionContextProvider,
) : HandlerTransaction<HpTerminalConfigRequest, HpTerminalConfigResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpTerminalConfigRequest): IsoMessage {
        Log.d("TAG", "buildMessage: dddddddddrt")
        Log.d("TAG", "buildMessage: dddddddddddddnooor")
        return terminalConfigMessageMessageBuilder.build()
    }

    override fun queueFailure(request: HpTerminalConfigRequest): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooora")

        val message = buildMessage(request)
        return HpTerminalConfigResult(
            detail = transport.map(
                transactionType = TransactionType.TERMINAL_CONFIG,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
                masterKey = "",
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        Log.d("TAG", "buildMessage: dddddddddddddnooorb")
        response?.getIsoMessage()?.dump(System.out, "oooo")
        return (response?.responseCode != "300")
    }
    // IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooord")

        return HpTerminalConfigResult(
            detail = transport.map(
                transactionType = TransactionType.TERMINAL_CONFIG,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
                masterKey = "",
            ),
        )
    }

    override fun success(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorg")

        return try {
            configurationStore.markConfigured()
            terminalConfigStore.markActivated()
            response?.let(::persistTerminalConfig)
            HpTerminalConfigResult(
                detail = transport.map(
                    transactionType = TransactionType.INIT,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                    masterKey = HpKeyConfig.MASTER_KEY_HEX,
                ),
            )
        } catch (error: Exception) {
            HpTerminalConfigResult(
                detail = transport.failureDetail(
                    transactionType = TransactionType.INIT,
                    sentMessage = sentMessage,
                    response = response,
                    responseCode = "96",
                    responseMessage = error.message.orEmpty().ifBlank { messages.failed() },
                ),
            )
        }
    }

    override fun connectFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorv")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.CONNECT_FAILED,
                responseMessage = transport.connectFailedMessage(error),
            ),
        )
    }

    override fun sendFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorm")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.TERMINAL_CONFIG,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.SEND_FAILED,
                responseMessage = transport.sendFailedMessage(error),
            ),
        )
    }

    override fun receiveFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorq")

        return HpTerminalConfigResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.RECEIVE_FAILED,
                responseMessage = transport.receiveFailedMessage(error),
            ),
        )
    }

    override fun networkError(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpTerminalConfigResult {
        Log.d("TAG", "buildMessage: dddddddddddddnooorm")

        return receiveFailure(request, sentMessage, e)
    }

    /**
     * طبق بخش 9.3 مستند پروتکل: به‌روزرسانی کامل هویت پایانه از DE41/DE42/DE43 پاسخ 1314
     * به دست می‌آید. مقادیر خالی، مقدار فعلاً ذخیره‌شده را حفظ می‌کنند تا در جاهای دیگر
     * پروژه (ساخت پیام‌های تراکنشی و صفحه پیکربندی پایانه) از آخرین اطلاعات معتبر استفاده شود.
     */
    private fun persistTerminalConfig(response: IsoMessage) {
        val current = contextProvider.getTerminalConfig()
        val terminalId = response.terminalId.trim().ifBlank { current.terminalId }
        val merchantId = response.merchantId.trim().ifBlank { current.merchantId }
        val merchantNameLocation = response.getIsoMessage().getString(43)?.trim().orEmpty()
        val updated = current.copy(
            terminalId = terminalId,
            merchantId = merchantId,
            merchantName = merchantNameLocation.ifBlank { current.merchantName },
        )
        if (updated != current) {
            contextProvider.saveTerminalConfig(updated)
        }
    }
}
