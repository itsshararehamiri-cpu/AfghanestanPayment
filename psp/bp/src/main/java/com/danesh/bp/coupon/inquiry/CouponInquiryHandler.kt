package com.danesh.bp.coupon.inquiry

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponInquiryHandler @Inject constructor(
    private val messageBuilder: BpCouponInquiryMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpCouponInquiryRequest, BpCouponInquiryResult ,IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: BpCouponInquiryRequest): IsoMessage {
        return runBlocking {
            messageBuilder.build(request)
        }
    }

    override fun queueFailure(request: BpCouponInquiryRequest): BpCouponInquiryResult {
        val message = buildMessage(request)
        return BpCouponInquiryResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_INQUIRY,
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
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponInquiryResult {
        return BpCouponInquiryResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_INQUIRY,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed()
            ),
        )
    }

    override fun success(
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponInquiryResult {
        return BpCouponInquiryResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_INQUIRY,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success()
            ),
        )
    }

    override fun connectFailure(
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponInquiryResult = BpCouponInquiryResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_INQUIRY,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponInquiryResult = BpCouponInquiryResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_INQUIRY,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponInquiryResult = BpCouponInquiryResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_INQUIRY,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpCouponInquiryRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpCouponInquiryResult = receiveFailure(request, sentMessage, e)
}
