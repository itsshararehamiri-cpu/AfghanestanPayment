package com.danesh.api

object DefaultPurchaseAmountDefaults {
    /** کف تراکنش شاپرک — ریال */
    const val MIN_AMOUNT_RIALS = 2_000L
    const val MAX_DIGITS = 12
    const val MAX_AMOUNT_RIALS = 999_999_999_999L
}

enum class DefaultPurchaseAmountValidationError {
    EMPTY,
    INVALID,
    BELOW_MINIMUM,
    TOO_MANY_DIGITS,
}

object DefaultPurchaseAmountRules {
    fun validate(raw: String): DefaultPurchaseAmountValidationError? {
        val digits = raw.filter(Char::isDigit)
        if (digits.isEmpty()) return DefaultPurchaseAmountValidationError.EMPTY
        if (digits.length > DefaultPurchaseAmountDefaults.MAX_DIGITS) {
            return DefaultPurchaseAmountValidationError.TOO_MANY_DIGITS
        }
        val value = digits.toLongOrNull()
            ?: return DefaultPurchaseAmountValidationError.INVALID
        if (value < DefaultPurchaseAmountDefaults.MIN_AMOUNT_RIALS) {
            return DefaultPurchaseAmountValidationError.BELOW_MINIMUM
        }
        return null
    }

    fun normalize(raw: String): Long {
        val digits = raw.filter(Char::isDigit)
            .take(DefaultPurchaseAmountDefaults.MAX_DIGITS)
        return digits.toLongOrNull()
            ?.coerceAtMost(DefaultPurchaseAmountDefaults.MAX_AMOUNT_RIALS)
            ?: 0L
    }

    fun formatDigits(amountRials: Long): String =
        amountRials.toString()

    fun formatDisplay(amountRials: Long): String =
        String.format("%,d", amountRials)

    /** نمایش در منوی پذیرنده: مبلغ فرمت‌شده یا «-» اگر غیرفعال/ست‌نشده. */
    fun displayForMerchantSettings(enabled: Boolean, storedAmountRials: Long?): String {
        if (!enabled) return "-"
        val amount = storedAmountRials ?: return "-"
        if (amount < DefaultPurchaseAmountDefaults.MIN_AMOUNT_RIALS) return "-"
        return formatDisplay(amount)
    }

    /** مبلغ پیش‌فرض برای صفحه خرید؛ فقط وقتی سرویس فعال و مقدار معتبر باشد. */
    fun resolveForPurchase(enabled: Boolean, storedAmountRials: Long?): String? {
        if (!enabled) return null
        val amount = storedAmountRials ?: return null
        if (amount < DefaultPurchaseAmountDefaults.MIN_AMOUNT_RIALS) return null
        return formatDigits(amount)
    }
}
