package com.danesh.sadad.bill

import com.danesh.api.BillInquiryOutput
import com.danesh.api.BillInquiryRequest
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadBillInquiryResult
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillInquiryHandler @Inject constructor(
    private val billInquiryMessageBuilder: SadadBillInquiryMessageBuilder,
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
) : HandlerTransaction<BillInquiryRequest, SadadBillInquiryResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val needAdvice: Boolean = false

    override fun buildMessage(request: BillInquiryRequest): IsoMessage =
        kotlinx.coroutines.runBlocking { billInquiryMessageBuilder.build(request) }

    override fun queueFailure(request: BillInquiryRequest): SadadBillInquiryResult {
        val message = buildMessage(request)
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = message,
            response = null,
            isSuccess = false,
            responseMessage = messages.queueFailed(),
        ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED)
        val enriched = overlayBill(detail, request, response = null)
        return SadadBillInquiryResult(
            inquiry = enriched,
            detail = enriched,
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillInquiryResult {
        val detail = overlayBill(
            transport.map(
                transactionType = TransactionType.BILL,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
            request,
            response,
        )
        return SadadBillInquiryResult(inquiry = detail, detail = detail)
    }

    override fun success(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillInquiryResult {
        val detail = overlayBill(
            transport.map(
                transactionType = TransactionType.BILL,
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success(),
            ),
            request,
            response,
        )
        return SadadBillInquiryResult(inquiry = detail, detail = detail)
    }

    override fun connectFailure(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        )
        val enriched = overlayBill(detail, request, response = null)
        return SadadBillInquiryResult(inquiry = enriched, detail = enriched)
    }

    override fun sendFailure(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        )
        val enriched = overlayBill(detail, request, response = null)
        return SadadBillInquiryResult(inquiry = enriched, detail = enriched)
    }

    override fun receiveFailure(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): SadadBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        )
        val enriched = overlayBill(detail, request, response = null)
        return SadadBillInquiryResult(inquiry = enriched, detail = enriched)
    }

    override fun networkError(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadBillInquiryResult = receiveFailure(request, sentMessage, e)

    /**
     * مبلغ قبض از DE4 پاسخ استعلام می‌آید؛ اگر نبود از Function Code 008 و در نهایت از شناسه پرداخت.
     */
    private fun overlayBill(
        detail: com.danesh.api.TransactionResultDetail,
        request: BillInquiryRequest,
        response: IsoMessage?,
    ): BillInquiryOutput {
        val fromSwitch = response?.amount?.filter(Char::isDigit).orEmpty().trimStart('0')
        val parsedIds = response?.let { message ->
            runCatching { SadadBillFields.parseField48(message.getIsoMessage().getString(48)) }
                .getOrNull()
        }
        val billId = parsedIds?.first?.ifBlank { request.billId } ?: request.billId
        val payId = parsedIds?.second?.ifBlank { request.payId } ?: request.payId
        // ترتیب: DE4 پاسخ ← Function Code 008 (داخل detail.amount) ← استخراج از شناسه پرداخت
        val amount = fromSwitch
            .ifBlank { detail.amount.filter(Char::isDigit).trimStart('0') }
            .ifBlank { SadadBillFields.amountFromPaymentId(payId).trimStart('0') }
            .ifBlank { "0" }
        return detail.copy(
            billId = billId,
            payId = payId,
            amount = amount,
        )
    }
}
