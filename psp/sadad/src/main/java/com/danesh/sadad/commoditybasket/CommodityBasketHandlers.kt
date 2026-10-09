package com.danesh.sadad.commoditybasket

import com.danesh.api.IsoResponseCodes
import com.danesh.api.TransactionRequest
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.engine.HandlerTransaction
import com.danesh.iso.IsoMessage
import com.danesh.sadad.SadadTxnResult
import com.danesh.sadad.util.Field63Parser
import com.danesh.sadad.util.SadadIsoHandlerSupport
import com.danesh.sadad.util.SadadTransactionMessages
import javax.inject.Inject
import javax.inject.Singleton

/** پایهٔ مشترک تراکنش‌های کالابرگ سداد؛ تفاوت‌ها: پیام، نوع تراکنش و برگشت/اعلامیه. */
abstract class SadadCommodityBasketHandler<Req : TransactionRequest>(
    private val messages: SadadTransactionMessages,
    private val transport: SadadIsoHandlerSupport,
    private val transactionType: TransactionType,
) : HandlerTransaction<Req, SadadTxnResult, IsoMessage>() {

    /** جزئیات اختصاصی پاسخ (مثلاً مبالغ استعلام) روی نتیجه. */
    protected open fun decorate(detail: TransactionResultDetail, response: IsoMessage?): TransactionResultDetail = detail

    override fun queueFailure(request: Req): SadadTxnResult = SadadTxnResult(
        detail = transport.map(
            transactionType = transactionType,
            request = buildMessage(request),
            response = null,
            isSuccess = false,
            responseMessage = messages.queueFailed(),
        ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
    )

    override fun isFailure(response: IsoMessage?): Boolean =
        IsoResponseCodes.isFailure(response?.responseCode)

    override fun failure(request: Req, sentMessage: IsoMessage, response: IsoMessage?): SadadTxnResult =
        SadadTxnResult(
            detail = transport.map(
                transactionType = transactionType,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
        )

    override fun success(request: Req, sentMessage: IsoMessage, response: IsoMessage?): SadadTxnResult =
        SadadTxnResult(
            detail = decorate(
                transport.map(
                    transactionType = transactionType,
                    request = sentMessage,
                    response = response,
                    isSuccess = true,
                    responseMessage = messages.success(),
                ),
                response,
            ),
        )

    override fun connectFailure(request: Req, sentMessage: IsoMessage, error: Exception): SadadTxnResult =
        transportFailure(sentMessage, TransactionTransportCodes.CONNECT_FAILED, transport.connectFailedMessage(error))

    override fun sendFailure(request: Req, sentMessage: IsoMessage, error: Exception): SadadTxnResult =
        transportFailure(sentMessage, TransactionTransportCodes.SEND_FAILED, transport.sendFailedMessage(error))

    override fun receiveFailure(request: Req, sentMessage: IsoMessage, error: Exception): SadadTxnResult =
        transportFailure(sentMessage, TransactionTransportCodes.RECEIVE_FAILED, transport.receiveFailedMessage(error))

    override fun networkError(request: Req, sentMessage: IsoMessage, e: Exception): SadadTxnResult =
        receiveFailure(request, sentMessage, e)

    private fun transportFailure(sentMessage: IsoMessage, code: String, message: String): SadadTxnResult =
        SadadTxnResult(
            detail = transport.failureDetail(
                transactionType = transactionType,
                sentMessage = sentMessage,
                response = null,
                responseCode = code,
                responseMessage = message,
            ),
        )
}

/** استعلام کالابرگ: بدون رمز، بدون برگشت/اعلامیه، در گزارش ثبت نمی‌شود. */
@Singleton
class CommodityBasketInquiryHandler @Inject constructor(
    private val builder: SadadCommodityBasketMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadCommodityBasketHandler<SadadCommodityBasketInquiryRequest>(
    messages,
    transport,
    TransactionType.COUPON_INQUIRY,
) {
    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val recordsLastSuccessReference: Boolean = false

    override fun buildMessage(request: SadadCommodityBasketInquiryRequest): IsoMessage =
        kotlinx.coroutines.runBlocking { builder.buildInquiry(request) }

    override fun decorate(detail: TransactionResultDetail, response: IsoMessage?): TransactionResultDetail {
        val blocks = runCatching { Field63Parser.parse(response?.privateUseField63.orEmpty()) }
            .getOrDefault(emptyList())
        val quote = SadadCommodityBasketCodec.parseQuote(blocks) ?: return detail.copy(
            isSuccess = false,
            responseMessage = MISSING_QUOTE_MESSAGE,
        )
        return detail.copy(
            amount = quote.transactionAmount.toString(),
            couponCreditRequired = quote.creditAmount.toString(),
            couponCashAmount = quote.cashAmount.toString(),
            couponTrackingNumber = quote.traceItem,
        )
    }

    private companion object {
        const val MISSING_QUOTE_MESSAGE = "پاسخ استعلام کالابرگ مبالغ را نداشت"
    }
}

/** خرید کالابرگ: با رمز، برگشت‌پذیر و دارای اعلامیه (Advice). */
@Singleton
class CommodityBasketSaleHandler @Inject constructor(
    private val builder: SadadCommodityBasketMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadCommodityBasketHandler<SadadCommodityBasketSaleRequest>(
    messages,
    transport,
    TransactionType.COUPON_PURCHASE,
) {
    override val isReversible: Boolean = true
    override val needReport: Boolean = true
    override val needAdvice: Boolean = true
    override val deferAdviceUntilReceipt: Boolean = true

    override fun buildMessage(request: SadadCommodityBasketSaleRequest): IsoMessage =
        kotlinx.coroutines.runBlocking { builder.buildSale(request) }

    override fun decorate(detail: TransactionResultDetail, response: IsoMessage?): TransactionResultDetail =
        detail
}

/** لغو استعلام کالابرگ (FC 058). */
@Singleton
class CommodityBasketCancelHandler @Inject constructor(
    private val builder: SadadCommodityBasketMessageBuilder,
    messages: SadadTransactionMessages,
    transport: SadadIsoHandlerSupport,
) : SadadCommodityBasketHandler<SadadCommodityBasketCancelRequest>(
    messages,
    transport,
    TransactionType.COUPON_INQUIRY,
) {
    override val isReversible: Boolean = false
    override val needReport: Boolean = false
    override val recordsLastSuccessReference: Boolean = false

    override fun buildMessage(request: SadadCommodityBasketCancelRequest): IsoMessage =
        kotlinx.coroutines.runBlocking { builder.buildCancel(request) }
}
