package com.danesh.hp.balance

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
class BalanceHandler @Inject constructor(
    private val balanceMessageBuilder: HpBalanceMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpBalanceRequest, HpBalanceResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: HpBalanceRequest): IsoMessage =
        balanceMessageBuilder.build(request)

    override fun queueFailure(request: HpBalanceRequest): HpBalanceResult {
        val message = buildMessage(request)
        return HpBalanceResult(
            detail = transport.map(
                transactionType = TransactionType.BALANCE,
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
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.map(
            transactionType = TransactionType.BALANCE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBalanceResult = HpBalanceResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BALANCE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpBalanceRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpBalanceResult = receiveFailure(request, sentMessage, e)
}
