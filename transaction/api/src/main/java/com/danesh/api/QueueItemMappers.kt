package com.danesh.api

import android.util.Log

fun QueueItem.toTransactionResultDetail(
    terminalConfig: TerminalConfig = TerminalConfig(
        terminalId = "",
        merchantId = "",
        merchantName = "",
        merchantPhone = "",
        nii = "",
        pointOfServiceEntryMode = "",
        currency = "",
    ),
): TransactionResultDetail {
    Log.d("TAG", "toTransactionResultDetail: ddddffrttt$responseCode")
    Log.d("TAG", "toTransactionResultDsetauil: gggg${responseCode?.toString().orEmpty().padStart(2, '0')}")
    val successful = SafStatuses.needsAdvice(status) || responseCode == 0
    val maskedPanValue = maskedPan.orEmpty().maskPanForDisplay()
    return TransactionResultDetail(
        isSuccess = successful,
        transactionType = type.toTransactionType(),
        terminalId = terminalId.ifBlank { terminalConfig.terminalId },
        merchantId = merchantId.ifBlank { terminalConfig.merchantId },
        merchantName = terminalConfig.merchantName,
        merchantPhone = terminalConfig.merchantPhone,
        englishMerchantName = terminalConfig.englishMerchantName,
        merchantAddress = terminalConfig.merchantAddress,
        merchantPostalCode = terminalConfig.merchantPostalCode,
        maskedPan = maskedPanValue,
        pan = maskedPanValue,
        trace = stan,
        rrn = rrn?.takeIf { it.isNotBlank() },
        date = date,
        time = time,
        dateTime = when {
            date.isNotBlank() && time.isNotBlank() -> "$date - $time"
            else -> dateTime
        },
       // responseCode = responseCode?.toString().orEmpty().padStart(2, '0'),
        responseCode = responseCode?.toString() ?: "-1",
        responseMessage = responseMsg.orEmpty(),
        amount = amount,
        issuerName = issuer.orEmpty(),
    )
}