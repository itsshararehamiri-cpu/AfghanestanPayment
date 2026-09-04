package com.danesh.common.receipt

interface MerchantReceiptPrintPreferences {
    fun getMode(): MerchantReceiptPrintMode
    fun setMode(mode: MerchantReceiptPrintMode)
    fun resetToDefaults()
}
