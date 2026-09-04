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
        billInquiryMessageBuilder.build(request)

    override fun queueFailure(request: BillInquiryRequest): SadadBillInquiryResult {
        val message = buildMessage(request)
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = message,
            response = null,
            isSuccess = false,
            responseMessage = messages.queueFailed(),
        ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED)
        return SadadBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillInquiryResult {
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        )
        return SadadBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
    }

    override fun success(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): SadadBillInquiryResult {
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        )
        return SadadBillInquiryResult(
            inquiry = mapInquiry(
                request = request,
                response = response,
                isSuccess = true,
                responseMessage = detail.responseMessage,
                responseCode = detail.responseCode,
            ),
            detail = detail,
        )
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
        return SadadBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
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
        return SadadBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
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
        return SadadBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
    }

    override fun networkError(
        request: BillInquiryRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): SadadBillInquiryResult = receiveFailure(request, sentMessage, e)

    private fun mapInquiry(
        request: BillInquiryRequest,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,
        responseCode: String,
    ): BillInquiryOutput {
        response?.unpackField48()
        val amountFromTag = response?.getField48Tag("857")?.filter { it.isDigit() }.orEmpty()
        val amountFromDe4 = response?.amount?.filter { it.isDigit() }.orEmpty()
        val billAmount = (amountFromTag.ifBlank { amountFromDe4 })
            .trimStart('0')
            .ifBlank { "0" }
        return BillInquiryOutput(
            isSuccess = isSuccess,
            responseCode = responseCode.ifBlank { response?.responseCode.orEmpty() },
            responseMessage = responseMessage,
            billId = response?.getField48Tag("850")?.ifBlank { request.billId } ?: request.billId,
            amount = billAmount,
        )
    }

    private fun failureOutput(
        request: BillInquiryRequest,
        responseCode: String,
        responseMessage: String,
    ): BillInquiryOutput = BillInquiryOutput(
        isSuccess = false,
        responseCode = responseCode,
        responseMessage = responseMessage,
        billId = request.billId,
    )
}
