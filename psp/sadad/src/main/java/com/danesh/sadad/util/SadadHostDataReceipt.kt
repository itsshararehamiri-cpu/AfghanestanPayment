package com.danesh.sadad.util

import com.danesh.api.TransactionResultDetail
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Host Function Codeهای مالی سداد را روی نتیجه تراکنش می‌نشاند:
 * - متن‌های مشتری (نام سازمان قبض، تخفیف، پیام برنده دارنده کارت) → hostReceiptTextSecond (هر دو رسید)
 * - پیام برنده پذیرنده → merchantReceiptText (فقط رسید پذیرنده)
 * - مبلغ استعلام قبض (008) وقتی DE4 خالی است → amount
 */
object SadadHostDataReceipt {

    fun apply(detail: TransactionResultDetail, host: SadadHostData): TransactionResultDetail {
        if (host.isEmpty) return detail
        val customerLines = buildList {
            host.billOrganization?.let { org ->
                org.nameFa.ifBlank { org.nameEn }.takeIf { it.isNotBlank() }?.let { add("سازمان: $it") }
            }
            host.discount?.let { discount ->
                add("مبلغ اصلی: ${formatRial(discount.originalAmount)}")
                add("مبلغ پس از تخفیف: ${formatRial(discount.cardHolderAmount)}")
            }
            host.cardHolderWinnerMessage?.takeIf { it.isNotBlank() }?.let(::add)
        }
        val second = listOfNotNull(
            detail.hostReceiptTextSecond?.takeIf { it.isNotBlank() },
            customerLines.takeIf { it.isNotEmpty() }?.joinToString("\n"),
        ).joinToString("\n").ifBlank { null }

        val inquiryAmount = host.billInquiry?.amount?.trimStart('0').orEmpty()
        val amount = if (detail.amount.trimStart('0').isBlank() && inquiryAmount.isNotBlank()) {
            inquiryAmount
        } else {
            detail.amount
        }
        return detail.copy(
            hostReceiptTextSecond = second,
            merchantReceiptText = host.merchantWinnerMessage?.takeIf { it.isNotBlank() }
                ?: detail.merchantReceiptText,
            amount = amount,
        )
    }

    internal fun formatRial(amount: String): String {
        val value = amount.trimStart('0').toLongOrNull() ?: 0L
        val format = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return "${format.format(value)} ریال"
    }
}
