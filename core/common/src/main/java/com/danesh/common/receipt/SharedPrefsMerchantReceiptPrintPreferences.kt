package com.danesh.common.receipt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsMerchantReceiptPrintPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : MerchantReceiptPrintPreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getMode(): MerchantReceiptPrintMode =
        MerchantReceiptPrintMode.fromStored(prefs.getString(KEY_MODE, null))

    override fun setMode(mode: MerchantReceiptPrintMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    override fun resetToDefaults() {
        prefs.edit().remove(KEY_MODE).apply()
    }

    companion object {
        private const val PREFS_NAME = "merchant_receipt_print_prefs"
        private const val KEY_MODE = "merchant_receipt_print_mode"
    }
}
