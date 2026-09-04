package com.danesh.hp.name_inquiry

import com.danesh.api.IsoResponseCodes
import com.danesh.api.NameInquiryOutput
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import com.danesh.engine.HandlerTransaction
import com.danesh.hp.util.HpIsoHandlerSupport
import com.danesh.hp.util.HpTransactionMessages
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NameInquiryHandler @Inject constructor(
    private val messageBuilder: HpNameInquiryMessageBuilder,
    private val messages: HpTransactionMessages,
    private val transport: HpIsoHandlerSupport,
) : HandlerTransaction<HpNameInquiryRequest, HpNameInquiryResult, IsoMessage>() {

    override val isReversible: Boolean = false
    override val needAdvice: Boolean = false
    override val needReport: Boolean = false

    override fun buildMessage(request: HpNameInquiryRequest): IsoMessage =
        messageBuilder.build(request)

    override fun queueFailure(request: HpNameInquiryRequest): HpNameInquiryResult {
        val message = buildMessage(request)
        val detail = enrichTransferDetail(
            request = request,
            detail = transport.map(
                transactionType = inquiryTransactionType(request),
                request = message,
                response = null,
                isSuccess = false,
                responseMessage = messages.queueFailed(),
            ).copy(responseCode = TransactionTransportCodes.QUEUE_BLOCKED),
        )
        return inquiryResult(request, detail, detail.rrn.orEmpty().ifBlank { message.rrn.orEmpty() })
    }

    override fun isFailure(response: IsoMessage?): Boolean {
        if (response == null || IsoResponseCodes.isFailure(response.responseCode)) return true
        response.unpackField48()
        return response.getField48Tag(HOLDER_NAME_TAG).isNullOrBlank()
    }

    override fun failure(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpNameInquiryResult {
        val detail = enrichTransferDetail(
            request = request,
            detail = transport.map(
                transactionType = inquiryTransactionType(request),
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = messages.failed(),
            ),
        )
        return inquiryResult(
            request = request,
            detail = detail,
            rrn = resolveRrn(detail.rrn, response, sentMessage),
            holderName = response?.getField48Tag(HOLDER_NAME_TAG).orEmpty(),
        )
    }

    override fun success(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        response: IsoMessage?,
    ): HpNameInquiryResult {
        val detail = enrichTransferDetail(
            request = request,
            detail = transport.map(
                transactionType = inquiryTransactionType(request),
                request = sentMessage,
                response = response,
                isSuccess = true,
                responseMessage = messages.success(),
            ),
        )
        response?.unpackField48()
        val holderName = response?.getField48Tag(HOLDER_NAME_TAG).orEmpty()
        return inquiryResult(
            request = request,
            detail = detail,
            rrn = resolveRrn(detail.rrn, response, sentMessage),
            holderName = holderName,
            isSuccess = holderName.isNotBlank(),
        )
    }

    override fun connectFailure(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpNameInquiryResult = transportFailure(
        request,
        sentMessage,
        TransactionTransportCodes.CONNECT_FAILED,
        transport.connectFailedMessage(error),
    )

    override fun sendFailure(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpNameInquiryResult = transportFailure(
        request,
        sentMessage,
        TransactionTransportCodes.SEND_FAILED,
        transport.sendFailedMessage(error),
    )

    override fun receiveFailure(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        error: Exception,
    ): HpNameInquiryResult = transportFailure(
        request,
        sentMessage,
        TransactionTransportCodes.RECEIVE_FAILED,
        transport.receiveFailedMessage(error),
    )

    override fun networkError(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        e: Exception,
    ): HpNameInquiryResult = receiveFailure(request, sentMessage, e)

    private fun transportFailure(
        request: HpNameInquiryRequest,
        sentMessage: IsoMessage,
        code: String,
        message: String,
    ): HpNameInquiryResult {
        val detail = enrichTransferDetail(
            request = request,
            detail = transport.failureDetail(
                transactionType = inquiryTransactionType(request),
                sentMessage = sentMessage,
                response = null,
                responseCode = code,
                responseMessage = message,
            ),
        )
        return inquiryResult(
            request = request,
            detail = detail,
            rrn = sentMessage.rrn.orEmpty(),
        )
    }

    private fun inquiryResult(
        request: HpNameInquiryRequest,
        detail: TransactionResultDetail,
        rrn: String = detail.rrn.orEmpty(),
        holderName: String = "",
        isSuccess: Boolean = false,
    ): HpNameInquiryResult {
        val inquiry = NameInquiryOutput(
            isSuccess = isSuccess,
            responseCode = detail.responseCode,
            responseMessage = detail.responseMessage,
            holderName = holderName,
            rrn = rrn,
            detail = detail,
        )
        return HpNameInquiryResult(inquiry = inquiry, detail = detail)
    }

    private fun inquiryTransactionType(request: HpNameInquiryRequest): TransactionType = when {
        request.forWalletToWallet -> TransactionType.WALLET_TO_WALLET
        request.forWallet -> TransactionType.CARD_TO_WALLET
        else -> TransactionType.CARD_TO_CARD
    }

    private fun enrichTransferDetail(
        request: HpNameInquiryRequest,
        detail: TransactionResultDetail,
    ): TransactionResultDetail {
        val digits = request.destination.filter { it.isDigit() }
        val sourceDigits = request.sourceWallet.filter { it.isDigit() }
        val panValue = when {
            request.forWalletToWallet -> detail.pan.ifBlank { sourceDigits }
            else -> detail.pan.ifBlank { request.pan }
        }
        return detail.copy(
            transactionType = inquiryTransactionType(request),
            pan = panValue,
            maskedPan = detail.maskedPan.ifBlank { panValue.maskPanForDisplay() },
            destinationPan = when {
                request.forWalletToWallet -> digits
                !request.forWallet -> digits
                else -> detail.destinationPan
            },
            walletCode = if (request.forWallet && !request.forWalletToWallet) {
                digits
            } else {
                detail.walletCode
            },
        )
    }

    private fun resolveRrn(
        detailRrn: String?,
        response: IsoMessage?,
        sentMessage: IsoMessage,
    ): String =
        detailRrn?.takeIf { it.isNotBlank() }
            ?: response?.rrn?.takeIf { it.isNotBlank() }
            ?: sentMessage.rrn.orEmpty()

    companion object {
        private const val HOLDER_NAME_TAG = "049"
    }
}
