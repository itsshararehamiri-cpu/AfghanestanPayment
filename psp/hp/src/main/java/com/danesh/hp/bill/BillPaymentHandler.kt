package com.danesh.hp.bill

import com.danesh.api.BillUserInput
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

typealias HpBillPaymentRequest = BillUserInput

@Singleton
class BillPaymentHandler @Inject constructor(
    private val messageBuilder: HpBillPaymentMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpBillPaymentRequest, HpBillPaymentResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true

    override fun buildMessage(request: HpBillPaymentRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: HpBillPaymentRequest): HpBillPaymentResult {
        val message = buildMessage(request)
        return HpBillPaymentResult(
            detail = transport.map(
                transactionType = TransactionType.BILL,
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
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBillPaymentResult = HpBillPaymentResult(
        detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
    )

    override fun success(
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBillPaymentResult = HpBillPaymentResult(
        detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
    )

    override fun connectFailure(
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillPaymentResult = HpBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillPaymentResult = HpBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillPaymentResult = HpBillPaymentResult(
        detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: HpBillPaymentRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpBillPaymentResult = receiveFailure(request, sentMessage, e)
}
