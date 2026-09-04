package com.danesh.hp.cash_out

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
class CashOutHandler @Inject constructor(
    private val cashOutMessageBuilder: HpCashOutMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpCashOutRequest, HpCashOutResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpCashOutRequest): IsoMessage =
        cashOutMessageBuilder.build(request)

    override fun queueFailure(request: HpCashOutRequest): HpCashOutResult {
        val message = buildMessage(request)
        return HpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
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
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCashOutResult {
        return HpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
        )
    }

    override fun success(
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpCashOutResult {
        return HpCashOutResult(
            detail = transport.map(
                transactionType = TransactionType.CASH_OUT,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success(),
            ),
        )
    }

    override fun connectFailure(
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashOutResult = HpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashOutResult = HpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpCashOutResult = HpCashOutResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.CASH_OUT,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpCashOutRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpCashOutResult = receiveFailure(request, sentMessage, e)
}
