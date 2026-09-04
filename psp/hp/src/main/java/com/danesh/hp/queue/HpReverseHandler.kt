package com.danesh.hp.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpReverseHandler @Inject constructor(
    private val reverseMessageBuilder: HpReverseMessageBuilder,
) : HandlerTransaction<HpReverseRequest, HpAdviceResult, IsoMessage>() {

    override val needReport: Boolean = false
    override val isReversible: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpReverseRequest): IsoMessage =
        reverseMessageBuilder.build(request.queueItem)

    override fun queueFailure(request: HpReverseRequest): HpAdviceResult {
        return HpAdviceResult(
            isSuccess = false,
            responseCode = TransactionTransportCodes.QUEUE_BLOCKED,
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = response?.responseCode.orEmpty(),
    )

    override fun success(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = true,
        responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
    )

    override fun connectFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.CONNECT_FAILED,
    )

    override fun sendFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.SEND_FAILED,
    )

    override fun receiveFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.RECEIVE_FAILED,
    )

    override fun networkError(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpAdviceResult = receiveFailure(request, sentMessage, e)
}
