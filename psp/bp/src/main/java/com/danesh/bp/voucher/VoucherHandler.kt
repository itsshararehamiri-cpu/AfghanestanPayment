package com.danesh.bp.voucher

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.bp.util.BpIsoHandlerSupport
import com.danesh.bp.util.BpTransactionMessages
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherHandler @Inject constructor(
    private val voucherMessageBuilder: BpVoucherMessageBuilder,
    private val messages: BpTransactionMessages,
    private val transport: BpIsoHandlerSupport,
) : HandlerTransaction<BpVoucherRequest, BpVoucherResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: BpVoucherRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            voucherMessageBuilder.build(request)
        }

    override fun queueFailure(request: BpVoucherRequest): BpVoucherResult {
        val message = buildMessage(request)
        return BpVoucherResult(
            detail = transport.map(
                transactionType = TransactionType.VOUCHER,
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
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpVoucherResult = BpVoucherResult(
        detail = transport.map(
            transactionType = TransactionType.VOUCHER,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed()
        ),
    )

    override fun success(
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): BpVoucherResult = BpVoucherResult(
        detail = transport.map(
            transactionType = TransactionType.VOUCHER,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success()
        ),
    )

    override fun connectFailure(
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpVoucherResult = BpVoucherResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpVoucherResult = BpVoucherResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): BpVoucherResult = BpVoucherResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.VOUCHER,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: BpVoucherRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): BpVoucherResult = receiveFailure(request, sentMessage, e)
}
