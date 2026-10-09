package com.danesh.api

/**
 * کف و سقف «رسید اختیاری» مشتری.
 *
 * - مبلغ کمتر از [lowerRials] → رسید مشتری لازم نیست.
 * - مبلغ بین کف و سقف → از کاربر پرسیده می‌شود.
 * - مبلغ بیشتر از [upperRials] → رسید مشتری اجباری است.
 */
data class OptionalReceiptLimits(
    val active: Boolean,
    val lowerRials: Long,
    val upperRials: Long,
) {
    companion object {
        const val MAX_AMOUNT_RIALS = 999_999_999_999L
    }
}

enum class CustomerReceiptMode {
    /** رسید مشتری چاپ نمی‌شود. */
    NONE,

    /** از کاربر پرسیده می‌شود. */
    ASK,

    /** رسید مشتری اجباری (چاپ خودکار). */
    MANDATORY,
}

enum class OptionalReceiptValidationError {
    EMPTY,
    INVALID,
    LOWER_GREATER_THAN_UPPER,
}

object OptionalReceiptRules {

    fun customerReceiptMode(limits: OptionalReceiptLimits?, amountRials: Long?): CustomerReceiptMode {
        if (limits == null || !limits.active || amountRials == null) return CustomerReceiptMode.MANDATORY
        return when {
            amountRials < limits.lowerRials -> CustomerReceiptMode.NONE
            amountRials > limits.upperRials -> CustomerReceiptMode.MANDATORY
            else -> CustomerReceiptMode.ASK
        }
    }

    fun validate(lower: String, upper: String): OptionalReceiptValidationError? {
        if (lower.isBlank() || upper.isBlank()) return OptionalReceiptValidationError.EMPTY
        val low = lower.filter(Char::isDigit).toLongOrNull() ?: return OptionalReceiptValidationError.INVALID
        val high = upper.filter(Char::isDigit).toLongOrNull() ?: return OptionalReceiptValidationError.INVALID
        if (low > OptionalReceiptLimits.MAX_AMOUNT_RIALS || high > OptionalReceiptLimits.MAX_AMOUNT_RIALS) {
            return OptionalReceiptValidationError.INVALID
        }
        if (low > high) return OptionalReceiptValidationError.LOWER_GREATER_THAN_UPPER
        return null
    }
}

/** نتیجهٔ ارسال کف/سقف به سوئیچ. */
data class OptionalReceiptUpdateResult(
    val isSuccess: Boolean,
    /** مقادیر نهایی اعمال‌شده (پاسخ سوئیچ) — در صورت شکست `null`. */
    val limits: OptionalReceiptLimits? = null,
    val responseCode: String = "",
    val responseMessage: String = "",
    /** PSP این قابلیت را پشتیبانی نمی‌کند. */
    val unsupported: Boolean = false,
)
