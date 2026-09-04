package com.danesh.hp.bill

import android.util.Log
import com.danesh.api.BillInquiryOutput
import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.balance.HpBalanceResult
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BillInquiryHandler @Inject constructor(
    private val billInquiryMessageBuilder: HpBillInquiryMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpBillInquiryRequest, HpBillInquiryResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val needAdvice: Boolean = false

    override fun buildMessage(request: HpBillInquiryRequest): IsoMessage =
        billInquiryMessageBuilder.build(request)

    override fun queueFailure(request: HpBillInquiryRequest): HpBillInquiryResult {
        val message = buildMessage(request)
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = message,
            response = null,
            isSuccess = false,
            responseMessage = messages.queueFailed(),
        ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED)
        return HpBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
    }

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBillInquiryResult {
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = false,
            responseMessage = messages.failed(),
        )

        return HpBillInquiryResult(
            inquiry = failureOutput(
                request = request,
                responseCode = detail.responseCode,
                responseMessage = detail.responseMessage,
            ),
            detail = detail,
        )
    }

    override fun success(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpBillInquiryResult {
        val detail = transport.map(
            transactionType = TransactionType.BILL,
            request = sentMessage,
            response = response,
            isSuccess = true,
            responseMessage = messages.success(),
        )
        val k= HpBillInquiryResult(
            inquiry = mapInquiry(
                request = request,
                response = response,
                isSuccess = true,
                responseMessage = detail.responseMessage,
                responseCode = detail.responseCode,
            ),
            detail = detail,
        )
        Log.d("TAG", "success:dmdmdmdmd $k")
        return k
    }

    override fun connectFailure(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.CONNECT_FAILED,
            responseMessage = transport.connectFailedMessage(error),
        )
        val v= HpBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
        Log.d("TAG", "connectFailure: dmdmdmdmd$v")
        return v
    }

    override fun sendFailure(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.SEND_FAILED,
            responseMessage = transport.sendFailedMessage(error),
        )
        val b= HpBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
        Log.d("TAG", "sendFailure: dmdmdmdmd$b")
        return b
    }

    override fun receiveFailure(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpBillInquiryResult {
        val detail = transport.failureDetail(
            transactionType = TransactionType.BILL,
            sentMessage = sentMessage,
            response = null,
            responseCode = TransactionTransportCodes.RECEIVE_FAILED,
            responseMessage = transport.receiveFailedMessage(error),
        )
        val v= HpBillInquiryResult(
            inquiry = failureOutput(request, detail.responseCode, detail.responseMessage),
            detail = detail,
        )
        Log.d("TAG", "receiveFailure: dmdmdmdmd$v")
        return v
    }

    override fun networkError(
        request: HpBillInquiryRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpBillInquiryResult {
        val v=receiveFailure(request, sentMessage, e)
        return v
    }

    private fun mapInquiry(
        request: HpBillInquiryRequest,
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
//            companyCode = response?.getField48Tag("858").orEmpty(),
//            payerName = response?.getField48Tag("860").orEmpty(),
//            requestId = response?.getField48Tag("898").orEmpty(),
//            remainingBalance = response?.getField48Tag("019").orEmpty(),
        )


    }

    private fun failureOutput(
        request: HpBillInquiryRequest,
        responseCode: String,
        responseMessage: String,
    ): BillInquiryOutput {
        val v=BillInquiryOutput(
            isSuccess = false,
            responseCode = responseCode,
            responseMessage = responseMessage,
            billId = request.billId,
        )
        Log.d("TAG", "failureOutput: dmdmdmdmd$v")
        return v
    }
}
