package com.danesh.common.merchant

import com.danesh.api.MicroPaymentIndexDefaults
import com.danesh.api.MicroPaymentIndexRules

data class MicroPaymentIndexConfig(
    val enabled: Boolean,
    val thresholdRials: Long,
) {
    companion object {
        val Default = MicroPaymentIndexConfig(
            enabled = false,
            thresholdRials = MicroPaymentIndexDefaults.DEFAULT_AMOUNT_RIALS,
        )

        fun from(preferences: MerchantDisplayPreferences): MicroPaymentIndexConfig =
            MicroPaymentIndexConfig(
                enabled = preferences.isMicroPaymentIndexEnabled(),
                thresholdRials = MicroPaymentIndexRules.resolve(preferences.getMicroPaymentIndexAmountRials()),
            )
    }
}
