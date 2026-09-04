package com.danesh.hp.cash_deposit

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashDepositHandler @Inject constructor(
    private val cashDepositMessageBuilder: HpCashDepositMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpCashDepositRequest, HpCashDepositResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpCashDepositRequest): IsoMessage =
        cashDepositMessageBuilder.build(request)

    override fun queueFailure(request: HpCashDepositRequest): HpCashDepositResult {
        val message = buildMessage(request)
        return HpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        return IsoResponseCodes.isFailure(response?.responseCode)
    }

    override fun failure(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCashDepositResult {
        return HpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
        )
    }

    override fun success(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCashDepositResult {
        return HpCashDepositResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_DEPOSIT,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success(),
            ),
        )
    }

    override fun connectFailure(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashDepositResult = HpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashDepositResult = HpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashDepositResult = HpCashDepositResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_DEPOSIT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpCashDepositRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpCashDepositResult = receiveFailure(request, sentMessage, e)
}
