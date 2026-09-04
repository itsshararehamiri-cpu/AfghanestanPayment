package com.danesh.api

object VatPercentageDefaults {
    const val DEFAULT_PERCENT = "10"
}

enum class VatPercentageValidationError {
    EMPTY,
    INVALID,
    OUT_OF_RANGE,
}

object VatPercentageRules {
    fun resolve(raw: String?): String {
        val stored = raw?.trim().orEmpty()
        return stored.ifEmpty { VatPercentageDefaults.DEFAULT_PERCENT }
    }

    fun validate(raw: String): VatPercentageValidationError? {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return VatPercentageValidationError.EMPTY
        val value = normalized.toIntOrNull() ?: return VatPercentageValidationError.INVALID
        if (value !in 1..100) return VatPercentageValidationError.OUT_OF_RANGE
        return null
    }

    fun normalize(raw: String): String {
        val value = raw.trim().toIntOrNull()?.coerceIn(1, 100)
            ?: VatPercentageDefaults.DEFAULT_PERCENT.toInt()
        return value.toString()
    }

    fun isVatConfigurationItem(title: String): Boolean {
        val normalized = title.replace('\u200c', ' ').trim()
        return normalized.contains("مالیات") && normalized.contains("ارزش افزوده")
    }
}
