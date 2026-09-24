package com.danesh.sadad.util

import android.util.Log

/**
 * Host Function Codes مالی سداد در DE63 پاسخ (سند «Sadad Switch Internal Function Code»، بخش 2).
 * هر کد اختیاری است؛ کدی که نیامده یا قابل پارس نیست null می‌ماند و بقیه کدها را خراب نمی‌کند.
 */
data class SadadHostData(
    /** 003: پیام برنده دارنده کارت (ایران‌سیستم هگز) — روی رسید مشتری. */
    val cardHolderWinnerMessage: String? = null,
    /** 004: پیام برنده پذیرنده (ایران‌سیستم هگز) — فقط روی رسید پذیرنده. */
    val merchantWinnerMessage: String? = null,
    /** 007: تخفیف. */
    val discount: Discount? = null,
    /** 008: پاسخ استعلام قبض. */
    val billInquiry: BillInquiry? = null,
    /** 018: درصد مالیات بر ارزش افزوده شارژ. */
    val topupVatPercent: Double? = null,
    /** 026: نام سازمان قبض. */
    val billOrganization: BillOrganization? = null,
    /** 029: داده چاپ (خام — ساختار در سند کامل نیست). */
    val printData: String? = null,
    /** 033: رسید اختیاری. */
    val optionalReceipt: OptionalReceipt? = null,
) {
    data class Discount(val originalAmount: String, val cardHolderAmount: String, val merchantAmount: String)

    data class BillInquiry(
        val amount: String,
        val referenceNumber: String,
        val date: String,
        val time: String,
        val posCondition: String,
        val systemTraceNo: String,
    )

    data class BillOrganization(val nameFa: String, val nameEn: String)

    data class OptionalReceipt(val isActive: Boolean, val lowerBoundAmount: String, val upperBoundAmount: String)

    val isEmpty: Boolean get() = this == SadadHostData()
}

object SadadHostFunctionCodes {

    const val CARD_HOLDER_WINNER = "003"
    const val MERCHANT_WINNER = "004"
    const val DISCOUNT = "007"
    const val BILL_INQUIRY = "008"
    const val TOPUP_HOST_DATA = "018"
    const val BILL_PAYMENT_EXTRA_DATA = "026"
    const val PRINT_DATA = "029"
    const val OPTIONAL_RECEIPT = "033"

    private const val TAG = "SadadHostFC"

    fun parse(field63: String?): SadadHostData {
        if (field63.isNullOrBlank()) return SadadHostData()
        val blocks = runCatching { Field63Parser.parse(field63) }
            .onFailure { Log.w(TAG, "DE63 parse failed: ${it.message}") }
            .getOrNull() ?: return SadadHostData()
        return parse(blocks)
    }

    fun parse(blocks: List<FunctionCodeData>): SadadHostData {
        var result = SadadHostData()
        for (block in blocks) {
            result = runCatching { apply(result, block) }
                .onFailure { Log.w(TAG, "function code ${block.code} ignored: ${it.message}") }
                .getOrDefault(result)
        }
        return result
    }

    private fun apply(current: SadadHostData, block: FunctionCodeData): SadadHostData {
        val data = block.data
        return when (block.code) {
            CARD_HOLDER_WINNER -> current.copy(cardHolderWinnerMessage = decodeText(data))
            MERCHANT_WINNER -> current.copy(merchantWinnerMessage = decodeText(data))
            DISCOUNT -> {
                val r = FixedReader(data)
                current.copy(
                    discount = SadadHostData.Discount(
                        originalAmount = r.digits(12),
                        cardHolderAmount = r.digits(12),
                        merchantAmount = r.digits(12),
                    ),
                )
            }
            BILL_INQUIRY -> {
                val r = FixedReader(data)
                current.copy(
                    billInquiry = SadadHostData.BillInquiry(
                        amount = r.digits(12),
                        referenceNumber = r.take(12),
                        date = r.take(8),
                        time = r.take(6),
                        posCondition = r.take(2),
                        systemTraceNo = r.take(6),
                    ),
                )
            }
            TOPUP_HOST_DATA -> {
                val r = FixedReader(data)
                val floatingPoint = r.digits(1).toInt()
                val vat = r.digits(7).toLong()
                current.copy(topupVatPercent = vat / Math.pow(10.0, floatingPoint.toDouble()))
            }
            BILL_PAYMENT_EXTRA_DATA -> {
                val r = FixedReader(data)
                val nameFa = decodeText(r.take(r.digits(3).toInt()))
                val nameEn = if (r.remaining >= 3) r.take(r.digits(3).toInt()).trim() else ""
                current.copy(billOrganization = SadadHostData.BillOrganization(nameFa, nameEn))
            }
            PRINT_DATA -> current.copy(printData = data)
            OPTIONAL_RECEIPT -> {
                val r = FixedReader(data)
                val active = r.take(1) == "1"
                current.copy(
                    optionalReceipt = if (active) {
                        SadadHostData.OptionalReceipt(true, r.digits(12), r.digits(12))
                    } else {
                        SadadHostData.OptionalReceipt(false, "", "")
                    },
                )
            }
            else -> current
        }
    }

    /** متن فارسی سداد: هگز ایران‌سیستم یا بایت خام. */
    private fun decodeText(data: String): String =
        IranSystemEncoding.fieldToUtf8(data.toByteArray(SadadField63Wire.CHARSET))

    private class FixedReader(private val data: String) {
        private var index = 0
        val remaining: Int get() = data.length - index

        fun take(length: Int): String {
            require(length >= 0 && index + length <= data.length) {
                "need $length chars at $index, have ${data.length}"
            }
            return data.substring(index, index + length).also { index += length }
        }

        fun digits(length: Int): String = take(length).also { value ->
            require(value.all(Char::isDigit)) { "expected $length digits, got '$value'" }
        }
    }
}
