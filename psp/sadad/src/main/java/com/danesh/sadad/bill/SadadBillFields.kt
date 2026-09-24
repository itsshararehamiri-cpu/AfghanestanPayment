package com.danesh.sadad.bill

import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData

/**
 * فیلدهای قبض سداد.
 *
 * DE48: Bill_ID ۱۳ رقم + Payment_ID ۱۳ رقم؛ کوتاه‌تر از ۱۳ از چپ با صفر پر می‌شود.
 * شناسه پرداخت: مبلغ (بدون ۳ رقم کم‌ارزش، حداکثر ۸) + کد سال (۱) + کد دوره (۲) + دو رقم کنترل.
 */
object SadadBillFields {
    private const val PAYMENT_ID_SUFFIX_LENGTH = 5
    private const val ZERO_AMOUNT = "000000000000"

    fun field48(billId: String, paymentId: String): String =
        padId(billId) + padId(paymentId)

    /**
     * Assumption: no in-repo ISO layout sends mobile/line/bill-id in a different DE48
     * shape. The inquiry builder only emits Bill_ID(13)+Payment_ID(13), so a single
     * collected identifier is mapped to Bill_ID and Payment_ID is left-padded zeros.
     */
    fun inquiryIdsFromSingleIdentifier(identifier: String): Pair<String, String> =
        identifier.filter(Char::isDigit) to ""

    /**
     * پاسخ استعلام: اگر DE48 حداقل ۲۶ رقم باشد، ۱۳+۱۳ را به شناسه قبض/پرداخت می‌شکند.
     */
    fun parseField48(raw: String?): Pair<String, String>? {
        val digits = raw.orEmpty().filter(Char::isDigit)
        if (digits.length < SadadKeyConfig.BILL_ID_LENGTH * 2) return null
        val billId = digits.take(SadadKeyConfig.BILL_ID_LENGTH).trimStart('0')
        val payId = digits.drop(SadadKeyConfig.BILL_ID_LENGTH)
            .take(SadadKeyConfig.BILL_ID_LENGTH)
            .trimStart('0')
        return billId to payId
    }

    fun amountFromPaymentId(paymentId: String): String {
        val digits = paymentId.filter(Char::isDigit).take(SadadKeyConfig.BILL_PAYMENT_ID_LENGTH)
        if (digits.length < 6) return ZERO_AMOUNT
        val major = digits.dropLast(PAYMENT_ID_SUFFIX_LENGTH)
        if (major.isEmpty() || major.all { it == '0' }) return ZERO_AMOUNT
        return (major + "000").padStart(12, '0').takeLast(12)
    }

    /**
     * DE4 پرداخت: مبلغ واردشده اگر غیرصفر باشد، وگرنه مبلغ استخراج‌شده از شناسه پرداخت.
     */
    fun resolvePaymentAmount(enteredAmount: String, paymentId: String): String {
        val entered = enteredAmount.filter(Char::isDigit)
        if (entered.isNotBlank() && entered.any { it != '0' }) {
            return entered.padStart(12, '0').takeLast(12)
        }
        return amountFromPaymentId(paymentId)
    }

    /** DE63 استعلام: یک Function Code برابر 008 و بدون داده. */
    fun inquiryField63(): String = Field63Generator.generate(
        listOf(FunctionCodeData(SadadKeyConfig.BILL_INQUIRY_FUNCTION_CODE, "")),
    )

    private fun padId(raw: String): String =
        raw.filter(Char::isDigit)
            .padStart(SadadKeyConfig.BILL_ID_LENGTH, '0')
            .takeLast(SadadKeyConfig.BILL_ID_LENGTH)
}
