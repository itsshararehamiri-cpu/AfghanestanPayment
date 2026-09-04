package com.danesh.settings.config
data class SettingsMenuVisibility(
    val showSupportServices: Boolean = true,
    val showVatPercentage: Boolean = true,
    val showTmsSection: Boolean = true,
    val showMerchantShowFee: Boolean = true,
    val showMerchantMicroPaymentIndex: Boolean = true,
    val showSupportMicroPaymentIndex: Boolean = true,
)

fun interface SettingsMenuVisibilityProvider {
    fun visibility(): SettingsMenuVisibility
}
