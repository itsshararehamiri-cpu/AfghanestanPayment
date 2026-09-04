package com.danesh.bp.cash_out

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashOutHandler @Inject constructor(
    private val messageBuilder: BpCashOutMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpCashOutRequest, BpCashOutResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: BpCashOutRequest): IsoMessage {
        return kotlinx.coroutines.runBlocking {
            messageBuilder.build(request)
        }
    }

    override fun queueFailure(request: BpCashOutRequest): BpCashOutResult {
        val message = buildMessage(request)
        return BpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed()
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        return IsoResponseCodes.isFailure(response?.responseCode)
    }

    override fun failure(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCashOutResult {
        return BpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed()
            ),
        )
    }

    override fun success(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCashOutResult {
        return BpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success()
            ),
        )
    }

    override fun connectFailure(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashOutResult = BpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashOutResult = BpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashOutResult = BpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpCashOutRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpCashOutResult = receiveFailure(request, sentMessage, e)
}
