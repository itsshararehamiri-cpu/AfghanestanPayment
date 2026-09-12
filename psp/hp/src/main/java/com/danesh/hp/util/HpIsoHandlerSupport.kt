package com.danesh.hp.util

import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.TransactionType
import com.danesh.common.strings.TransportErrorNormalizer
import com.danesh.hp.diagnostics.DiagnosticRecord
import com.danesh.hp.diagnostics.HpDiagnosticLogRecorder
import com.danesh.iso.IsoMessage
import com.danesh.hp.iso.HpIsoMessageFactory
import com.danesh.iso.IsoTransactionResultMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpIsoHandlerSupport @Inject constructor(
    private val isoMessageFactory: HpIsoMessageFactory,
    private val messages: HpTransactionMessages,
    private val resultMapper: IsoTransactionResultMapper,
    private val diagnosticLog: HpDiagnosticLogRecorder,
    private val responseCodeText: HpResponseCodeText,
) {
    fun map(
        transactionType: TransactionType,
        request: IsoMessage,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,masterKey: String=""
    ): TransactionResultDetail {
       val detail = TransportErrorNormalizer.normalize(
           resultMapper.map(
               transactionType = transactionType,
               request = request,
               response = response,
               isSuccess = isSuccess,
               responseMessage = responseMessage,
               merchantId = isoMessageFactory.terminalMerchantId(),
               merchantName = isoMessageFactory.terminalMerchantName(),
               merchantPhone = isoMessageFactory.merchantPhone(), masterKey = masterKey
           ),
           messages.appStrings,
       ).let { base ->
           base.copy(
               receiptHeaderText = isoMessageFactory.receiptHeaderText(),
               receiptFooterText = isoMessageFactory.receiptFooterText(),
               responseMessage = mappedFailureMessage(base),
           )
       }
       recordDiagnostics(request, response, detail)
       return detail
    }

    /** طبق رفتار رسید: کنار کد پاسخ ناموفق، توصیف نگاشت‌شدهٔ DE39 نمایش داده شود. */
    private fun mappedFailureMessage(detail: TransactionResultDetail): String {
        if (detail.isSuccess) return detail.responseMessage
        if (TransactionTransportCodes.isTransportCode(detail.responseCode)) return detail.responseMessage
        return responseCodeText.describe(detail.responseCode) ?: detail.responseMessage
    }

    /** فقط جهت پیام، MTI، STAN/RRN، نتیجهٔ انتقال و DE39 — هرگز PAN/Track2/PIN. */
    private fun recordDiagnostics(
        request: IsoMessage,
        response: IsoMessage?,
        detail: TransactionResultDetail,
    ) {
        val correlationId = listOf(request.stan, response?.rrn.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(separator = "/")
        diagnosticLog.record(
            DiagnosticRecord(
                direction = DiagnosticRecord.Direction.OUT,
                mti = request.mti,
                correlationId = correlationId,
                transportOutcome = if (detail.isSuccess) "OK" else detail.responseCode,
                de39 = response?.responseCode.orEmpty(),
            ),
        )
    }

    fun failureDetail(
        transactionType: TransactionType,
        sentMessage: IsoMessage,
        response: IsoMessage?,
        responseCode: String,
        responseMessage: String,
    ): TransactionResultDetail =
        TransportErrorNormalizer.normalize(
            map(
                transactionType = transactionType,
                request = sentMessage,
                response = response,
                isSuccess = false,
                responseMessage = responseMessage, masterKey = ""
            ).copy(responseCode = TransactionTransportCodes.normalizeCode(responseCode)),
            messages.appStrings,
        )

    fun connectFailedMessage(error: Exception): String = messages.connectFailed()

    fun sendFailedMessage(error: Exception): String = messages.sendFailed()

    fun receiveFailedMessage(error: Exception): String = messages.receiveFailed()
}
