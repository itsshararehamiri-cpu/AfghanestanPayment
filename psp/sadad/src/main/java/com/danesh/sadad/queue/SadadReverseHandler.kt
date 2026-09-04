package com.danesh.sadad.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadReverseHandler @Inject constructor(
    private val reverseMessageBuilder: SadadReverseMessageBuilder,
) : HandlerTransaction<SadadReverseRequest, SadadAdviceResult, IsoMessage>() {

    override val needReport: Boolean = false
    override val isReversible: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: SadadReverseRequest): IsoMessage =
        reverseMessageBuilder.build(request.queueItem)

    override fun queueFailure(request: SadadReverseRequest): SadadAdviceResult =
        SadadAdviceResult(isSuccess = false, responseCode = TransactionTransportCodes.QUEUE_BLOCKED)

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = response?.responseCode.orEmpty(),
    )

    override fun success(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = true,
        responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
    )

    override fun connectFailure(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.CONNECT_FAILED,
    )

    override fun sendFailure(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.SEND_FAILED,
    )

    override fun receiveFailure(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadAdviceResult = SadadAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.RECEIVE_FAILED,
    )

    override fun networkError(
        request: SadadReverseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadAdviceResult = receiveFailure(request, sentMessage, e)
}
