package com.danesh.hp.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.PspDeviceOperations
import com.danesh.api.TerminalConfig
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
    private val messageBuilder: HpTerminalConfigMessageHandler,
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

    override fun buildMessage(request: HpTerminalConfigRequest): IsoMessage =
        messageBuilder.build()

    override fun queueFailure(request: HpTerminalConfigRequest): HpTerminalConfigResult {
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

    override fun isFailure(response: IsoMessage?): Boolean = response?.responseCode != "300"

    override fun failure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult = HpTerminalConfigResult(
        detail = transport.map(
            transactionType = TransactionType.TERMINAL_CONFIG,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
            masterKey = "",
        ),
    )

    override fun success(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpTerminalConfigResult {
        return try {
            persistTerminalProfile(response)
            configurationStore.markConfigured()
            terminalConfigStore.markActivated()
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
    ): HpTerminalConfigResult = HpTerminalConfigResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult = HpTerminalConfigResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.TERMINAL_CONFIG,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpTerminalConfigResult = HpTerminalConfigResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.INIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpTerminalConfigRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpTerminalConfigResult = receiveFailure(request, sentMessage, e)

    /**
     * پاسخ موفق 1314 — طبق سند پروتکل DE41 (شماره پایانه)، DE42 (شماره پذیرنده) و DE43
     * (نام/مکان پذیرنده، فقط جهت نمایش/رسید) و DE26 (MCC) ممکن است برگردند؛ این مقادیر
     * روی پروفایل محلی ذخیره می‌شوند تا تراکنش‌های بعدی از آن‌ها استفاده کنند. DE43 هرگز
     * در درخواست خروجی echo نمی‌شود.
     */
    private fun persistTerminalProfile(response: IsoMessage?) {
        if (response == null) return

        val current = contextProvider.getTerminalConfig()
        val terminalId = response.terminalId.trim().ifBlank { current.terminalId }
        val merchantId = response.merchantId.trim().ifBlank { current.merchantId }
        val merchantNameLocation = response.merchantNameLocation.trim()
        val merchantName = merchantNameLocation.ifBlank { current.merchantName }

        val updated: TerminalConfig = current.copy(
            terminalId = terminalId,
            merchantId = merchantId,
            merchantName = merchantName,
        )
        if (updated != current) {
            contextProvider.saveTerminalConfig(updated)
        }

        val mcc = response.mcc.trim()
        if (mcc.isNotBlank()) {
            terminalConfigStore.saveMcc(mcc)
        }
    }
}
