package com.danesh.hp.queue

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.diagnostics.DiagnosticRecord
import com.danesh.hp.diagnostics.HpDiagnosticLogRecorder
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpReverseHandler @Inject constructor(
    private val reverseMessageBuilder: HpReverseMessageBuilder,
    private val diagnosticLog: HpDiagnosticLogRecorder,
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
    ): HpAdviceResult {
        recordReversalOutcome(sentMessage, response, transportOutcome = response?.responseCode.orEmpty(), outcome = "FAILED")
        return HpAdviceResult(
            isSuccess = false,
            responseCode = response?.responseCode.orEmpty(),
        )
    }

    override fun success(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpAdviceResult {
        recordReversalOutcome(sentMessage, response, transportOutcome = "OK", outcome = "CONFIRMED")
        return HpAdviceResult(
            isSuccess = true,
            responseCode = response?.responseCode.orEmpty().ifEmpty { "00" },
        )
    }

    override fun connectFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult {
        recordReversalOutcome(sentMessage, null, TransactionTransportCodes.CONNECT_FAILED, outcome = "RETRY")
        return HpAdviceResult(
            isSuccess = false,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
        )
    }

    override fun sendFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult {
        recordReversalOutcome(sentMessage, null, TransactionTransportCodes.SEND_FAILED, outcome = "RETRY")
        return HpAdviceResult(
            isSuccess = false,
            responseCode = TransactionTransportCodes.SEND_FAILED,
        )
    }

    override fun receiveFailure(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpAdviceResult {
        recordReversalOutcome(sentMessage, null, TransactionTransportCodes.RECEIVE_FAILED, outcome = "RETRY")
        return HpAdviceResult(
            isSuccess = false,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
        )
    }

    override fun networkError(
        request: HpReverseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpAdviceResult = receiveFailure(request, sentMessage, e)

    /** جهت پیام، MTI=1420، STAN/RRN، نتیجهٔ انتقال، DE39 و نتیجهٔ Reverse — هرگز PAN/Track2/PIN. */
    private fun recordReversalOutcome(
        sentMessage: IsoMessage,
        response: IsoMessage?,
        transportOutcome: String,
        outcome: String,
    ) {
        val correlationId = listOf(sentMessage.stan, response?.rrn.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(separator = "/")
        diagnosticLog.record(
            DiagnosticRecord(
                direction = DiagnosticRecord.Direction.OUT,
                mti = sentMessage.mti,
                correlationId = correlationId,
                transportOutcome = transportOutcome,
                de39 = response?.responseCode.orEmpty(),
                reversalOutcome = outcome,
            ),
        )
    }
}
