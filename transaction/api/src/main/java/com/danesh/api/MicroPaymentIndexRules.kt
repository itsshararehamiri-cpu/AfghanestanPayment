package com.danesh.api

object MicroPaymentIndexDefaults {
    const val DEFAULT_AMOUNT_RIALS = 5_000_000L
    const val MIN_AMOUNT_RIALS = 1L
    const val MAX_AMOUNT_RIALS = 999_999_999_999L
}

enum class MicroPaymentIndexValidationError {
    EMPTY,
    INVALID,
    OUT_OF_RANGE,
}

object MicroPaymentIndexRules {
    fun resolve(stored: Long?): Long =
        stored?.takeIf { it in MicroPaymentIndexDefaults.MIN_AMOUNT_RIALS..MicroPaymentIndexDefaults.MAX_AMOUNT_RIALS }
            ?: MicroPaymentIndexDefaults.DEFAULT_AMOUNT_RIALS

    fun validate(raw: String): MicroPaymentIndexValidationError? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return MicroPaymentIndexValidationError.EMPTY
        val value = normalized.filter(Char::isDigit).toLongOrNull()
            ?: return MicroPaymentIndexValidationError.INVALID
        if (value !in MicroPaymentIndexDefaults.MIN_AMOUNT_RIALS..MicroPaymentIndexDefaults.MAX_AMOUNT_RIALS) {
            return MicroPaymentIndexValidationError.OUT_OF_RANGE
        }
        return null
    }

    fun normalize(raw: String): Long {
        val digits = raw.filter(Char::isDigit)
        val value = digits.toLongOrNull()?.coerceIn(
            MicroPaymentIndexDefaults.MIN_AMOUNT_RIALS,
            MicroPaymentIndexDefaults.MAX_AMOUNT_RIALS,
        )
        return value ?: MicroPaymentIndexDefaults.DEFAULT_AMOUNT_RIALS
    }

    fun formatDisplay(amountRials: Long): String =
        String.format("%,d", amountRials)
}

fun TransactionResultDetail.amountRials(): Long? =
    amount.filter(Char::isDigit).toLongOrNull()
