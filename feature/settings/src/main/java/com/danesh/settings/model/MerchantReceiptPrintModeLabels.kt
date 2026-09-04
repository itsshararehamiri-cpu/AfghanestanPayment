package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.common.receipt.MerchantReceiptPrintMode
import com.danesh.settings.R

@StringRes
fun MerchantReceiptPrintMode.labelRes(): Int = when (this) {
    MerchantReceiptPrintMode.OPTIONAL -> R.string.settings_merchant_receipt_optional
    MerchantReceiptPrintMode.MANDATORY -> R.string.settings_merchant_receipt_mandatory
    MerchantReceiptPrintMode.DISABLED -> R.string.settings_merchant_receipt_disabled
}
