package com.danesh.common.merchant

import android.content.Context
import com.danesh.api.MicroPaymentIndexDefaults
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsMerchantDisplayPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : MerchantDisplayPreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isShowFeeEnabled(): Boolean =
        prefs.getBoolean(KEY_SHOW_FEE, DEFAULT_SHOW_FEE)

    override fun setShowFeeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_FEE, enabled).apply()
    }

    override fun isMicroPaymentIndexEnabled(): Boolean =
        prefs.getBoolean(KEY_MICRO_PAYMENT_INDEX, DEFAULT_MICRO_PAYMENT_INDEX)

    override fun setMicroPaymentIndexEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MICRO_PAYMENT_INDEX, enabled).apply()
    }

    override fun getMicroPaymentIndexAmountRials(): Long? {
        if (!prefs.contains(KEY_MICRO_PAYMENT_INDEX_AMOUNT)) return null
        return prefs.getLong(KEY_MICRO_PAYMENT_INDEX_AMOUNT, MicroPaymentIndexDefaults.DEFAULT_AMOUNT_RIALS)
    }

    override fun setMicroPaymentIndexAmountRials(amountRials: Long) {
        prefs.edit().putLong(KEY_MICRO_PAYMENT_INDEX_AMOUNT, amountRials).apply()
    }

    override fun isDefaultPurchaseAmountEnabled(): Boolean =
        prefs.getBoolean(KEY_DEFAULT_PURCHASE_AMOUNT_ENABLED, DEFAULT_DEFAULT_PURCHASE_AMOUNT)

    override fun setDefaultPurchaseAmountEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEFAULT_PURCHASE_AMOUNT_ENABLED, enabled).apply()
    }

    override fun getDefaultPurchaseAmountRials(): Long? {
        if (!prefs.contains(KEY_DEFAULT_PURCHASE_AMOUNT)) return null
        return prefs.getLong(KEY_DEFAULT_PURCHASE_AMOUNT, 0L)
    }

    override fun setDefaultPurchaseAmountRials(amountRials: Long) {
        prefs.edit().putLong(KEY_DEFAULT_PURCHASE_AMOUNT, amountRials).apply()
    }

    override fun clearDefaultPurchaseAmountRials() {
        prefs.edit().remove(KEY_DEFAULT_PURCHASE_AMOUNT).apply()
    }

    override fun resetToDefaults() {
        prefs.edit()
            .remove(KEY_SHOW_FEE)
            .remove(KEY_MICRO_PAYMENT_INDEX)
            .remove(KEY_MICRO_PAYMENT_INDEX_AMOUNT)
            .remove(KEY_DEFAULT_PURCHASE_AMOUNT_ENABLED)
            .remove(KEY_DEFAULT_PURCHASE_AMOUNT)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "merchant_display_prefs"
        private const val KEY_SHOW_FEE = "show_fee"
        private const val KEY_MICRO_PAYMENT_INDEX = "micro_payment_index"
        private const val KEY_MICRO_PAYMENT_INDEX_AMOUNT = "micro_payment_index_amount"
        private const val KEY_DEFAULT_PURCHASE_AMOUNT_ENABLED = "default_purchase_amount_enabled"
        private const val KEY_DEFAULT_PURCHASE_AMOUNT = "default_purchase_amount"
        private const val DEFAULT_SHOW_FEE = false
        private const val DEFAULT_MICRO_PAYMENT_INDEX = false
        private const val DEFAULT_DEFAULT_PURCHASE_AMOUNT = false
    }
}
