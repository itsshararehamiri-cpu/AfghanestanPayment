package com.danesh.sadad.commoditybasket

import com.danesh.api.CouponOrderItem
import com.danesh.sadad.util.FunctionCodeData

/** نتیجهٔ استعلام کالابرگ: همان سه مقداری که در خرید (FC 047) برگردانده می‌شود. */
data class SadadCommodityBasketQuote(
    /** مبلغ کل تراکنش (ریال). */
    val transactionAmount: Long,
    /** مبلغی که از اعتبار کالابرگ پرداخت می‌شود (ریال). */
    val creditAmount: Long,
    /** شماره پیگیری کالابرگ (n13). */
    val traceItem: String,
) {
    /** مبلغی که باید از کارت پرداخت شود. */
    val cashAmount: Long get() = (transactionAmount - creditAmount).coerceAtLeast(0)
}

/**
 * کدگذاری DE63 کالابرگ سداد.
 *
 * FC 046 (استعلام): `n3` تعداد اقلام + برای هر قلم: بارکد `n13` + کد واحد `n2` + تعداد `n6` (دو رقم اعشار).
 * چیدمان از نمونهٔ سند گرفته شده است:
 * `002` `6619988178317 01 000100` `6655463344853 01 000200` (= یک و دو عدد).
 *
 * FC 047 (خرید / پاسخ استعلام): مبلغ تراکنش `n12` + مبلغ اعتبار `n12` + شماره پیگیری `n13`.
 * FC 058 (لغو): شماره پیگیری `n13`.
 */
object SadadCommodityBasketCodec {
    const val INQUIRY_FUNCTION_CODE = "046"
    const val SALE_FUNCTION_CODE = "047"
    const val CANCEL_FUNCTION_CODE = "058"

    private const val BARCODE_LENGTH = 13
    private const val UNIT_LENGTH = 2
    private const val QUANTITY_LENGTH = 6
    private const val QUOTE_LENGTH = 12 + 12 + 13

    fun inquiryData(items: List<CouponOrderItem>): String {
        require(items.isNotEmpty()) { "commodity basket has no items" }
        require(items.size <= 999) { "too many commodity basket items: ${items.size}" }
        return buildString {
            append(items.size.toString().padStart(3, '0'))
            items.forEach { item ->
                append(digits(item.commodityCode, BARCODE_LENGTH))
                append(digits(item.unitCode.ifBlank { "01" }, UNIT_LENGTH))
                append(digits((item.quantity.coerceAtLeast(1) * 100).toString(), QUANTITY_LENGTH))
            }
        }
    }

    fun saleData(transactionAmount: Long, creditAmount: Long, traceItem: String): String =
        digits(transactionAmount.toString(), 12) + digits(creditAmount.toString(), 12) + digits(traceItem, 13)

    fun cancelData(traceItem: String): String = digits(traceItem, 13)

    /** سه مقدار استعلام از DE63 پاسخ؛ اگر نیامده یا نامعتبر بود `null`. */
    fun parseQuote(blocks: List<FunctionCodeData>): SadadCommodityBasketQuote? {
        val candidate = blocks.firstOrNull { it.code == SALE_FUNCTION_CODE }
            ?: blocks.firstOrNull { it.code == INQUIRY_FUNCTION_CODE && looksLikeQuote(it.data) }
            ?: blocks.firstOrNull { looksLikeQuote(it.data) }
            ?: return null
        val data = candidate.data.trim()
        if (!looksLikeQuote(data)) return null
        return SadadCommodityBasketQuote(
            transactionAmount = data.substring(0, 12).toLong(),
            creditAmount = data.substring(12, 24).toLong(),
            traceItem = data.substring(24, QUOTE_LENGTH),
        )
    }

    private fun looksLikeQuote(data: String): Boolean {
        val trimmed = data.trim()
        return trimmed.length == QUOTE_LENGTH && trimmed.all(Char::isDigit)
    }

    private fun digits(raw: String, length: Int): String =
        raw.filter(Char::isDigit).padStart(length, '0').takeLast(length)
}
