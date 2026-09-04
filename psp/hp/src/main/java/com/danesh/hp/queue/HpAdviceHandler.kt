package com.danesh.hp.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpAdviceHandler @Inject constructor(
    private val adviceMessageBuilder: HpAdviceMessageBuilder,
) :
    HandlerTransaction<HpAdviceRequest, HpAdviceResult, IsoMessage>() {

    override val needReport: Boolean = false
    override val isReversible: Boolean = false
    override val skipQueueFlush: Boolean = true

    override fun buildMessage(request: HpAdviceRequest): IsoMessage =
        adviceMessageBuilder.build(request.queueItem)

    override fun queueFailure(request: HpAdviceRequest): HpAdviceResult {
        return HpAdviceResult(isSuccess = false, responseCode = TransactionTransportCodes.QUEUE_BLOCKED)
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        return IsoResponseCodes.isFailure(response?.responseCode)
    }

    override fun failure(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpAdviceResult {
        return HpAdviceResult(
            isSuccess = false,
            responseCode = response?.responseCode.orEmpty(),
        )
    }

    override fun success(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpAdviceResult {
        return HpAdviceResult(
            isSuccess = true,
            responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
        )
    }

    override fun connectFailure(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.CONNECT_FAILED,
    )

    override fun sendFailure(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.SEND_FAILED,
    )

    override fun receiveFailure(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult = HpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.RECEIVE_FAILED,
    )

    override fun networkError(
        request: HpAdviceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpAdviceResult = receiveFailure(request, sentMessage, e)
}
