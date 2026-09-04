package com.danesh.sadad.util

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionRequest
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadTxnResult

open class SadadDetailHandler<Req : TransactionRequest>(
    private val type: TransactionType,
    override val isReversible: Boolean,
    override val needReport: Boolean,
    private val build: (Req) -> IsoMessage,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
    override val needAdvice: Boolean = false,
    override val skipQueueFlush: Boolean = false,
) : HandlerTransaction<Req, SadadTxnResult, IsoMessage>() {

    override fun buildMessage(request: Req): IsoMessage = build(request)

    override fun queueFailure(request: Req): SadadTxnResult {
        val message = buildMessage(request)
        return SadadTxnResult(
            detail = transport.map(
                transactionType = type,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: Req,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.map(
            transactionType = type,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: Req,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.map(
            transactionType = type,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: Req,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = type,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: Req,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = type,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: Req,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadTxnResult = SadadTxnResult(
        detail = transport.failureDetail(
            transactionType = type,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: Req,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadTxnResult = receiveFailure(request, sentMessage, e)
}
