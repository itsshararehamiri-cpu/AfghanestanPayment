package com.danesh.bp.voucher

import kotlin.math.roundToLong

object BpTopUpAmountCalculator {

    fun parseChargeAmount(amount: String): Long =
        amount.filter(Char::isDigit).toLongOrNull()?.coerceAtLeast(0L) ?: 0L

    fun parseVatPercent(vatPercentRaw: String): Double {
        val normalized = vatPercentRaw.trim().replace(',', '.')
        if (normalized.isEmpty()) return 0.0
        return normalized.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    }

    fun vatAmount(chargeAmount: Long, vatPercentRaw: String): Long {
        if (chargeAmount <= 0L) return 0L
        val vatPercent = parseVatPercent(vatPercentRaw)
        if (vatPercent <= 0.0) return 0L
        return (chargeAmount * vatPercent / 100.0).roundToLong()
    }

    fun totalWithVat(chargeAmount: Long, vatPercentRaw: String): Long {
        if (chargeAmount <= 0L) return 0L
        return chargeAmount + vatAmount(chargeAmount, vatPercentRaw)
    }

    fun formatIsoAmount(amount: Long): String =
        amount.toString().padStart(12, '0').takeLast(12)
}
