package com.danesh.bp.init

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.core.Device
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.iso.requireBp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitHandler @Inject constructor(
    private val initMessageBuilder: BpInitMessageBuilder,
    private val responseProcessor: BpInitResponseProcessor,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
    private val device: Device
) : HandlerTransaction<BpInitRequest, BpInitResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val skipQueueFlush: Boolean = true
    override val recordsLastSuccessReference: Boolean = false

    override fun buildMessage(request: BpInitRequest): IsoMessage {
        return kotlinx.coroutines.runBlocking {
            initMessageBuilder.build(request)
        }
    }
    override fun queueFailure(request: BpInitRequest): BpInitResult {
        BpInitTrace.step("InitHandler", "queueFailure — صف مسدود است")
        val message = buildMessage(request)
        responseProcessor.clearSession(message.stan)
        wipeInitSensitiveIso(message, null)
        return BpInitResult(
            detail = transport.map(
                transactionType = TransactionType.INIT,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(), masterKey = "rrr"
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: BpInitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpInitResult {
        BpInitTrace.step("InitHandler", "failure rc=${response?.responseCode}")
        responseProcessor.clearSession(sentMessage.stan)
        wipeInitSensitiveIso(sentMessage, response)
        return BpInitResult(
            detail = transport.map(
                transactionType = TransactionType.INIT,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(), masterKey = "ytt"
            ),
        )
    }

    override fun success(
        request: BpInitRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpInitResult = try {
        BpInitTrace.step("InitHandler", "success — پردازش پاسخ")
        if (response != null) {
            kotlinx.coroutines.runBlocking {
                device.getCheckValue("donay")
                responseProcessor.processSuccess(request, sentMessage, response)
                device.getCheckValue("didan")
            }
        } else {
            responseProcessor.clearSession(sentMessage.stan)
            wipeInitSensitiveIso(sentMessage, null)
        }
        BpInitResult(
            detail = transport.map(
                transactionType = TransactionType.INIT,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success()
            ),
        )
    } catch (error: Exception) {
        BpInitTrace.error("InitHandler | خطا در success", error)
        responseProcessor.clearSession(sentMessage.stan)
        wipeInitSensitiveIso(sentMessage, response)
        BpInitResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = response,
                responseCode = "96",
                responseMessage = error.message.orEmpty().ifBlank { messages.failed() },
            ),
        )
    }

    override fun connectFailure(
        request: BpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpInitResult {
        BpInitTrace.step("InitHandler", "connectFailure")
        responseProcessor.clearSession(sentMessage.stan)
        wipeInitSensitiveIso(sentMessage, null)
        return BpInitResult(
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
        request: BpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpInitResult {
        BpInitTrace.step("InitHandler", "sendFailure")
        responseProcessor.clearSession(sentMessage.stan)
        wipeInitSensitiveIso(sentMessage, null)
        return BpInitResult(
            detail = transport.failureDetail(
                transactionType = TransactionType.INIT,
                sentMessage = sentMessage,
                response = null,
                responseCode = TransactionTransportCodes.SEND_FAILED,
                responseMessage = transport.sendFailedMessage(error),
            ),
        )
    }

    override fun receiveFailure(
        request: BpInitRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpInitResult {
        BpInitTrace.step("InitHandler", "receiveFailure")
        responseProcessor.clearSession(sentMessage.stan)
        wipeInitSensitiveIso(sentMessage, null)
        return BpInitResult(
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
        request: BpInitRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpInitResult {
        BpInitTrace.step("InitHandler", "networkError")
        return receiveFailure(request, sentMessage, e)
    }

    private fun wipeInitSensitiveIso(request: IsoMessage?, response: IsoMessage?) {
        runCatching { request?.requireBp()?.unsetFields(61, 62) }
        runCatching { response?.requireBp()?.unsetFields(61, 62) }
    }
}
