package com.danesh.bp.fee

import kotlin.math.roundToLong

object BpPurchaseFeeRules {
    const val LOW_AMOUNT_THRESHOLD = 6_000_000L
    const val HIGH_AMOUNT_THRESHOLD = 8_000_000L
    const val LOW_TIER_FEE = 1_200L
    const val HIGH_TIER_FEE = 160_000L
    const val MID_TIER_PERCENT = 0.02

    fun feeForAmount(amount: Long): Long {
        if (amount <= 0L) return 0L
        return when {
            amount < LOW_AMOUNT_THRESHOLD -> LOW_TIER_FEE
            amount > HIGH_AMOUNT_THRESHOLD -> HIGH_TIER_FEE
            else -> (amount * MID_TIER_PERCENT / 100.0).roundToLong()
        }
    }
}
