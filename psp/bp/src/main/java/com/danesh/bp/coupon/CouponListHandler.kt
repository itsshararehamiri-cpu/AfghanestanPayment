package com.danesh.bp.coupon

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
class CouponListHandler @Inject constructor(
    private val messageBuilder: BpCouponListMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpCouponListRequest, BpCouponListResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: BpCouponListRequest): IsoMessage {
        return runBlocking {
            messageBuilder.build(request)
        }
    }

    override fun queueFailure(request: BpCouponListRequest): BpCouponListResult {
        val message = buildMessage(request)
        return BpCouponListResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_LIST,
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
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponListResult {
        return BpCouponListResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_LIST,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed()
            ),
        )
    }

    override fun success(
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpCouponListResult {
        return BpCouponListResult(
            detail = transport.map(
                transactionType = TransactionType.COUPON_LIST,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success()
            ),
        )
    }

    override fun connectFailure(
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponListResult = BpCouponListResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_LIST,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponListResult = BpCouponListResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_LIST,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpCouponListResult = BpCouponListResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.COUPON_LIST,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpCouponListRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpCouponListResult = receiveFailure(request, sentMessage, e)
}
