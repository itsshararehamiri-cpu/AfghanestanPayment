package com.danesh.hp.purchase

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
class PurchaseHandler @Inject constructor(
    private val purchaseMessageBuilder: HpPurchaseMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpPurchaseRequest, HpPurchaseResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needAdvice: Boolean = false
    override val needReport: Boolean = true

    override fun buildMessage(request: HpPurchaseRequest): IsoMessage =
        purchaseMessageBuilder.build(request)

    override fun queueFailure(request: HpPurchaseRequest): HpPurchaseResult {
        val message = buildMessage(request)
        return HpPurchaseResult(
            detail = transport.map(
                transactionType = TransactionType.PURCHASE,
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
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpPurchaseResult = HpPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpPurchaseResult = HpPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpPurchaseResult = HpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpPurchaseResult = HpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpPurchaseResult = HpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpPurchaseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpPurchaseResult = receiveFailure(request, sentMessage, e)
}
