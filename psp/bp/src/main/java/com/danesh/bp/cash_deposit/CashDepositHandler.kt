package com.danesh.bp.cash_deposit

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
class CashDepositHandler @Inject constructor(
    private val messageBuilder: BpCashDepositMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpCashDepositRequest, BpCashDepositResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: BpCashDepositRequest): IsoMessage {
        return kotlinx.coroutines.runBlocking {
            messageBuilder.build(request)
        }
    }

    override fun queueFailure(request: BpCashDepositRequest): BpCashDepositResult {
        val message = buildMessage(request)
        return BpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
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
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCashDepositResult {
        return BpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed()
            ),
        )
    }

    override fun success(
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCashDepositResult {
        return BpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success()
            ),
        )
    }

    override fun connectFailure(
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashDepositResult = BpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashDepositResult = BpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCashDepositResult = BpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpCashDepositRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpCashDepositResult = receiveFailure(request, sentMessage, e)
}
