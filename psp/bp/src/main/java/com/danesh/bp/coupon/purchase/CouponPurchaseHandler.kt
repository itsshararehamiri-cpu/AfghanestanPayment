package com.danesh.bp.coupon.purchase

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * خرید کالابرگ — دقیقاً مشابه یک خرید معمولی (Purchase) با این تفاوت که فیلد 44
 * (شماره پیگیری کالابرگ) در درخواست ([BpCouponPurchaseMessageBuilder]) درج می‌شود و باید
 * در Reverse/Confirm این تراکنش نیز حفظ شود — نگاه کنید به
 * [com.danesh.bp.BpTransactionStore] و [com.danesh.bp.queue.BpReverseMessageBuilder] /
 * [com.danesh.bp.queue.BpAdviceMessageBuilder].
 */
@Singleton
class CouponPurchaseHandler @Inject constructor(
    private val purchaseMessageBuilder: BpCouponPurchaseMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpCouponPurchaseRequest, BpCouponPurchaseResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: BpCouponPurchaseRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            purchaseMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpCouponPurchaseRequest): BpCouponPurchaseResult {
        val message = buildMessage(request)
        return BpCouponPurchaseResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_PURCHASE,
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
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponPurchaseResult = BpCouponPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.COUPON_PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponPurchaseResult = BpCouponPurchaseResult(
        detail = transport.map(
            transactionType = TransactionType.COUPON_PURCHASE,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponPurchaseResult = BpCouponPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponPurchaseResult = BpCouponPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponPurchaseResult = BpCouponPurchaseResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_PURCHASE,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpCouponPurchaseRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpCouponPurchaseResult = receiveFailure(request, sentMessage, e)
}
