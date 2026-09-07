package com.danesh.sadad.purchase
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseHandler @Inject constructor(
  private val purchaseMessageBuilder: SadadPurchaseMessageBuilder,
  private val messages: SadadTransactionMessages,
 private val  transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadPurchaseRequest, SadadPurchaseResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: SadadPurchaseRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            purchaseMessageBuilder.build(request)
        }

    override fun queueFailure(request: SadadPurchaseRequest): SadadPurchaseResult {
        val message = buildMessage(request)
        return SadadPurchaseResult(
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
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadPurchaseResult = SadadPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()

        ),
    )

    override fun success(
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadPurchaseResult = SadadPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadPurchaseResult = SadadPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadPurchaseResult = SadadPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadPurchaseResult = SadadPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadPurchaseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadPurchaseResult = receiveFailure(request, sentMessage, e)
}

