package com.danesh.sadad.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadAdviceHandler @Inject constructor(
    private val adviceMessageBuilder: SadadAdviceMessageBuilder,
) : HandlerTransaction<SadadAdviceRequest, SadadAdviceResult, IsoMessage>() {

    override val needReport: Boolean = false
    override val isReversible: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: SadadAdviceRequest): IsoMessage =
        adviceMessageBuilder.build(request.queueItem)

    override fun queueFailure(request: SadadAdviceRequest): SadadAdviceResult =
        SadadAdviceResult(isSuccess = false, responseCode = TransactionTransportCodes.QUEUE_BLOCKED)

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = response?.responseCode.orEmpty(),
    )

    override fun success(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = true,
        responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
    )

    override fun connectFailure(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.CONNECT_FAILED,
    )

    override fun sendFailure(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.SEND_FAILED,
    )

    override fun receiveFailure(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.RECEIVE_FAILED,
    )

    override fun networkError(
        request: SadadAdviceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadAdviceResult = receiveFailure(request, sentMessage, e)
}
