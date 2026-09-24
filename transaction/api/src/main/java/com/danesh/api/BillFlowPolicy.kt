package com.danesh.api

/**
 * نوع قبض برای تراکنش جداگانهٔ استعلام قبض سداد.
 * HP/BP این فهرست را خالی می‌گذارند.
 */
enum class BillInquiryKind {
    MOBILE,
    TELECOM,
    WATER_ELECTRICITY,
}

enum class BillPaymentFieldError {
    EMPTY,
    INVALID,
    CHECK_DIGIT,
    TYPE,
    PAIR,
}

/**
 * نتیجهٔ اعتبارسنجی شناسه قبض/پرداخت.
 * null از [BillFlowPolicy.validateBillPayment] یعنی این PSP این الگوریتم را ندارد.
 */
data class BillPaymentValidation(
    val billIdError: BillPaymentFieldError? = null,
    val paymentIdError: BillPaymentFieldError? = null,
    val amount: String = "0",
    val billType: Int? = null,
) {
    val isValid: Boolean get() = billIdError == null && paymentIdError == null
}

/**
 * سیاست UI/تراکنش پرداخت قبض:
 * - BP: مبلغ از کاربر + یک تراکنش مالی
 * - HP: بدون مبلغ ورودی + استعلام (Info) سپس پرداخت (Save)
 * - Sadad: بدون مبلغ ورودی + بدون استعلام؛ DE4 از شناسه پرداخت
 */
interface BillFlowPolicy {
    val requiresAmountInput: Boolean

    /**
     * همراه‌پی: کارت‌کشی (PAN) → استعلام (Info) → تایید → کارت‌کشی دوباره → پین → پرداخت (Save).
     * به‌پرداخت: false — مبلغ → کارت‌کشی → پین → یک تراکنش مالی.
     * سداد: false — شناسه قبض/پرداخت → کارت‌کشی → پین → پرداخت (بدون استعلام).
     */
    val requiresInquiry: Boolean

    /**
     * فقط سداد: آیتم منوی جداگانهٔ «استعلام قبض».
     * مسیر پرداخت قبض را استعلام نمی‌کند.
     */
    val hasStandaloneInquiryTransaction: Boolean
        get() = false

    /** انواع قبض استعلام مستقل؛ فقط سداد پر است تا HP/BP منو/صفحهٔ جدید نبینند. */
    val standaloneInquiryTypes: List<BillInquiryKind>
        get() = emptyList()

    /**
     * سداد: رقم کنترل شناسه قبض و پرداخت، نوع قبض، و مبلغ از شناسه پرداخت.
     * بقیه PSPها null برمی‌گردانند و همان بررسی خالی‌نبودن می‌ماند.
     */
    fun validateBillPayment(billId: String, paymentId: String): BillPaymentValidation? = null
}
