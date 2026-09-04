package com.danesh.bp.purchase

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
class PurchaseHandler @Inject constructor(
    private val purchaseMessageBuilder: BpPurchaseMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpPurchaseRequest, BpPurchaseResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: BpPurchaseRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            purchaseMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpPurchaseRequest): BpPurchaseResult {
        val message = buildMessage(request)
        return BpPurchaseResult(
            detail = transport.map(
                transactionType = TransactionType.PURCHASE,
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed()
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpPurchaseResult = BpPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()

        ),
    )

    override fun success(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpPurchaseResult = BpPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpPurchaseResult = BpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpPurchaseResult = BpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpPurchaseResult = BpPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpPurchaseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpPurchaseResult = receiveFailure(request, sentMessage, e)
}
