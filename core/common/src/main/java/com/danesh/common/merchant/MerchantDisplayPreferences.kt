package com.danesh.common.merchant

interface MerchantDisplayPreferences {
    fun isShowFeeEnabled(): Boolean
    fun setShowFeeEnabled(enabled: Boolean)
    fun isMicroPaymentIndexEnabled(): Boolean
    fun setMicroPaymentIndexEnabled(enabled: Boolean)
    fun getMicroPaymentIndexAmountRials(): Long?
    fun setMicroPaymentIndexAmountRials(amountRials: Long)
    fun isDefaultPurchaseAmountEnabled(): Boolean
    fun setDefaultPurchaseAmountEnabled(enabled: Boolean)
    fun getDefaultPurchaseAmountRials(): Long?
    fun setDefaultPurchaseAmountRials(amountRials: Long)
    fun clearDefaultPurchaseAmountRials()
    fun resetToDefaults()
}
