package com.danesh.bp.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionSessionClock
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpAdviceHandler @Inject constructor(
    private val adviceMessageBuilder: BpAdviceMessageBuilder,
    private val sessionClock: TransactionSessionClock,
) : HandlerTransaction<BpAdviceRequest, BpAdviceResult, IsoMessage>() {

    override val needReport: Boolean = false
    override val isReversible: Boolean = false
    override val skipQueueFlush: Boolean = true
    override val recordsLastSuccessReference: Boolean = false

    override fun buildMessage(request: BpAdviceRequest): IsoMessage {
        val item = request.queueItem
        sessionClock.capture(TransactionClock(date = item.date, time = item.time))
        return kotlinx.coroutines.runBlocking {
            adviceMessageBuilder.build(item)
        }
    }

    override fun queueFailure(request: BpAdviceRequest): BpAdviceResult {
        return BpAdviceResult(isSuccess = false, responseCode = TransactionTransportCodes.QUEUE_BLOCKED)
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        return IsoResponseCodes.isFailure(response?.responseCode)
    }

    override fun failure(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpAdviceResult {
        return BpAdviceResult(
            isSuccess = false,
            responseCode = response?.responseCode.orEmpty(),
        )
    }

    override fun success(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpAdviceResult {
        return BpAdviceResult(
            isSuccess = true,
            responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
        )
    }

    override fun connectFailure(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpAdviceResult = BpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.CONNECT_FAILED,
    )

    override fun sendFailure(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpAdviceResult = BpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.SEND_FAILED,
    )

    override fun receiveFailure(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpAdviceResult = BpAdviceResult(
        isSuccess = false,
        responseCode = TransactionTransportCodes.RECEIVE_FAILED,
    )

    override fun networkError(
        request: BpAdviceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpAdviceResult = receiveFailure(request, sentMessage, e)
}
