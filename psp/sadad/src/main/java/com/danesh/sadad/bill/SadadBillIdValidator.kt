package com.danesh.sadad.bill

import com.danesh.api.BillPaymentFieldError
import com.danesh.api.BillPaymentValidation

/**
 * اعتبارسنجی شناسه قبض و شناسه پرداخت سداد، مطابق pubCheckBillId / pubCheckPaymentId.
 *
 * رقم کنترل از رقم ماقبل آخر به چپ با وزن‌های ۲..۷ و سپس دوباره ۲ محاسبه می‌شود.
 * اگر `11 - (sum % 11)` بزرگ‌تر از ۹ باشد، رقم کنترل صفر است.
 */
object SadadBillIdValidator {
    private const val MIN_LENGTH = 6
    private const val MAX_LENGTH = 13
    private const val BILL_ID_PADDED_LENGTH = 13

    fun validate(billId: String, paymentId: String): BillPaymentValidation {
        val billCode = checkBillId(billId)
        val billError = billIdError(billId, billCode)
        if (billError != null) {
            return BillPaymentValidation(billIdError = billError)
        }
        val paymentCode = checkPaymentId(paymentId, billId.trim())
        val paymentError = paymentIdError(paymentId, paymentCode)
        if (paymentError != null) {
            return BillPaymentValidation(
                paymentIdError = paymentError,
                billType = billCode,
            )
        }
        return BillPaymentValidation(
            amount = SadadBillFields.amountFromPaymentId(paymentId),
            billType = billCode,
        )
    }

    /** نوع قبض از رقم ماقبل آخر شناسهٔ اصلی (بدون پد). نامعتبر: -4. */
    fun billType(billId: String): Int {
        if (billId.length < 2) return -4
        return when (billId[billId.length - 2]) {
            '0' -> 0
            '1' -> 1
            '2' -> 2
            '3' -> 3
            '4' -> 4
            '5' -> 5
            '6', '7' -> 6
            '8' -> 8
            '9' -> 9
            else -> -4
        }
    }

    /**
     * true یعنی رقم کنترل با رقم آخر نمی‌خواند (همان مقدار غیرصفر checkDigitBillPayment).
     */
    internal fun checkDigitMismatch(input: String): Boolean {
        if (input.length < 2 || input.any { !it.isDigit() }) return true
        var weight = 2
        var sum = 0
        for (index in input.length - 2 downTo 0) {
            if (weight == 8) weight = 2
            sum += (input[index] - '0') * weight
            weight++
        }
        val remainder = sum % 11
        val expected = if (11 - remainder > 9) 0 else 11 - remainder
        return expected != (input.last() - '0')
    }

    private fun checkBillId(billId: String): Int {
        val id = billId.trim()
        val length = id.length
        if (length < MIN_LENGTH || length > MAX_LENGTH) return -1
        if (id.any { !it.isDigit() } || id.all { it == '0' }) return -1
        val padded = id.padStart(BILL_ID_PADDED_LENGTH, '0')
        if (checkDigitMismatch(padded)) return -2
        return billType(id)
    }

    private fun checkPaymentId(paymentId: String, billId: String): Int {
        val id = paymentId.trim()
        val length = id.length
        if (length < MIN_LENGTH || length > MAX_LENGTH) return -5
        if (id.any { !it.isDigit() } || id.all { it == '0' }) return -5
        if (checkDigitMismatch(id.dropLast(1))) return -6
        if (checkDigitMismatch(billId + id)) return -7
        return 0
    }

    private fun billIdError(raw: String, code: Int): BillPaymentFieldError? = when (code) {
        -1 -> if (raw.trim().isEmpty()) BillPaymentFieldError.EMPTY else BillPaymentFieldError.INVALID
        -2 -> BillPaymentFieldError.CHECK_DIGIT
        -4 -> BillPaymentFieldError.TYPE
        else -> null
    }

    private fun paymentIdError(raw: String, code: Int): BillPaymentFieldError? = when (code) {
        -5 -> if (raw.trim().isEmpty()) BillPaymentFieldError.EMPTY else BillPaymentFieldError.INVALID
        -6 -> BillPaymentFieldError.CHECK_DIGIT
        -7 -> BillPaymentFieldError.PAIR
        else -> null
    }
}
