package com.danesh.iso

import android.util.Log
import com.danesh.api.maskPanForDisplay
import com.danesh.api.IsoResponseCodes
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionSessionClock
import com.danesh.api.TransactionType
import com.danesh.api.parseTransactionClockFromField12
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsoTransactionResultMapper @Inject constructor(
    private val sessionClock: TransactionSessionClock,
) {

    fun map(
        transactionType: TransactionType,
        request: IsoMessage,
        response: IsoMessage?,
        isSuccess: Boolean,
        responseMessage: String,
        merchantId: String,
        merchantName: String,
        merchantPhone: String,masterKey: String
    ): TransactionResultDetail {
        val source = response ?: request
        val rawPan = source.pan.ifBlank { request.pan }
        val maskedPanValue = rawPan.maskPanForDisplay()
        val (date, time) = resolveDateTime(request)
        val responseCode = source.responseCode.orEmpty().trim()
        val resolvedSuccess = when {
            response != null -> IsoResponseCodes.isApproved(responseCode)
            else -> isSuccess
        }
        val resolvedMerchantId = merchantId
            .ifBlank { source.merchantId }
            .ifBlank { request.merchantId }
        val payId = request.getField48Tag("021").orEmpty().trim()

        return TransactionResultDetail(
            isSuccess = resolvedSuccess,
            transactionType = transactionType,
            terminalId = source.terminalId.ifBlank { request.terminalId },
            merchantId = resolvedMerchantId,
            merchantName = merchantName.ifBlank { resolvedMerchantId },
            pan = maskedPanValue,
            maskedPan = maskedPanValue,
            trace = source.stan.ifBlank { request.stan },
            rrn = source.rrn?.takeIf { it.isNotBlank() },
            date = date,
            time = time,
            dateTime = buildDateTime(date, time),
            responseCode = responseCode.ifBlank {
                if (resolvedSuccess) "000" else ""
            },
            responseMessage = responseMessage,
            amount = source.amount.ifBlank { request.amount },
            availableBalance = response?.getField48Tag("019").orEmpty(),
            issuerName = "",
            posCode = source.pointOfServiceEntryMode.ifBlank { request.pointOfServiceEntryMode },
            merchantPhone = merchantPhone,
            masterkey = masterKey,
            payId = payId,
        )
    }

    private fun resolveDateTime(request: IsoMessage): Pair<String, String> {
        sessionClock.current()?.let { return it.date to it.time }
        parseTransactionClockFromField12(request.dateTime)?.let { return it.date to it.time }
        return "" to ""
    }

    private fun buildDateTime(date: String, time: String): String = when {
        date.isNotBlank() && time.isNotBlank() -> "$date - $time"
        date.isNotBlank() -> date
        time.isNotBlank() -> time
        else -> ""
    }
}
