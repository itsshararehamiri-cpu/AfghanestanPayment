package com.danesh.common.qr

import android.net.Uri
import com.danesh.api.TerminalConfig
import com.danesh.common.currency.formatAmountWithCurrencyLabel
import java.util.Locale

object QrPaymentContent {

    fun buildPurchaseQrContent(terminalId: String, amount: String): String {
        val encodedTerminalId = Uri.encode(terminalId)
        val encodedAmount = Uri.encode(amount.replace(",", "").trim())
        return "pay://terminal/$encodedTerminalId?amount=$encodedAmount"
    }

    fun buildBillQrContent(terminalId: String, billId: String, paymentId: String): String {
        val encodedTerminalId = Uri.encode(terminalId)
        val encodedBillId = Uri.encode(billId.trim())
        val encodedPaymentId = Uri.encode(paymentId.trim())
        return "pay://terminal/$encodedTerminalId?billId=$encodedBillId&paymentId=$encodedPaymentId"
    }

    fun formatPayableAmount(amount: String, currencyLabel: String): String {
        val normalized = amount.replace(",", "").trim()
        if (normalized.isEmpty() || normalized == "—") {
            return formatAmountWithCurrencyLabel("—", currencyLabel)
        }
        val numericAmount = normalized.toLongOrNull()
        val formatted = numericAmount?.let { String.format(Locale.US, "%,d", it) } ?: normalized
        return formatAmountWithCurrencyLabel(formatted, currencyLabel)
    }

    fun buildDetails(
        config: TerminalConfig,
        transactionTypeLabel: String,
        payableAmount: String,
        currencyLabel: String,
        merchantDisplayName: String? = null,
    ): QrPaymentDetails {
        val merchantName = merchantDisplayName ?: config.merchantName.ifBlank { config.merchantId }
        return QrPaymentDetails(
            paymentProviderName = merchantName,
            transactionType = transactionTypeLabel,
            merchantName = merchantName,
            payableAmount = formatPayableAmount(payableAmount, currencyLabel),
        )
    }
}
