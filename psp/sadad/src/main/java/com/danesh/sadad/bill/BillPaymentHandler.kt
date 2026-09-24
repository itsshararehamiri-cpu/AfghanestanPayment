package com.danesh.sadad.bill

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.util.IranSystemEncoding
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillPaymentHandler @Inject constructor(
    private val billPaymentMessageBuilder: SadadBillPaymentMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
) : HandlerTransaction<SadadBillPaymentRequest, SadadBillPaymentResult, IsoMessage>() {

    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: SadadBillPaymentRequest): IsoMessage =
        kotlinx.coroutines.runBlocking {
            billPaymentMessageBuilder.build(request)
        }

    override fun queueFailure(request: SadadBillPaymentRequest): SadadBillPaymentResult {
        val message = buildMessage(request)
        return finish(
            request,
            transport.map(
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
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillPaymentResult = finish(
        request,
        transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        ),
        response,
    )

    override fun success(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillPaymentResult = finish(
        request,
        transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        ),
        response,
    )

    override fun connectFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = finish(
        request,
        transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        ),
    )

    override fun sendFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = finish(
        request,
        transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        ),
    )

    override fun receiveFailure(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillPaymentResult = finish(
        request,
        transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        ),
    )

    override fun networkError(
        request: SadadBillPaymentRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadBillPaymentResult = receiveFailure(request, sentMessage, e)

    private fun finish(
        request: SadadBillPaymentRequest,
        detail: TransactionResultDetail,
        response: IsoMessage? = null,
    ): SadadBillPaymentResult = SadadBillPaymentResult(
        detail = overlayBill(detail, request, response),
    )

    /**
     * DE44 پاسخ پرداخت، متن چاپ به فرمت ایران‌سیستم است.
     */
    private fun overlayBill(
        detail: TransactionResultDetail,
        request: SadadBillPaymentRequest,
        response: IsoMessage?,
    ): TransactionResultDetail {
        val printText = response?.additionalResponseData
            ?.takeIf { it.isNotBlank() }
            ?.let { IranSystemEncoding.fieldToUtf8(it.toByteArray(Charsets.ISO_8859_1)) }
            .orEmpty()
        return detail.copy(
            billId = request.billId.ifBlank { detail.billId },
            payId = request.payId.ifBlank { detail.payId },
            hostReceiptText = printText.ifBlank { detail.hostReceiptText },
        )
    }
}
