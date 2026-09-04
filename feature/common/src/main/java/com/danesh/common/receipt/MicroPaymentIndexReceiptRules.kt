package com.danesh.common.receipt

object MicroPaymentIndexReceiptRules {
    fun resolveEffectivePrintMode(
        baseMode: MerchantReceiptPrintMode,
        microPaymentIndexEnabled: Boolean,
        thresholdRials: Long,
        transactionAmountRials: Long?,
    ): MerchantReceiptPrintMode {
        if (!microPaymentIndexEnabled || transactionAmountRials == null) {
            return baseMode
        }
        return if (transactionAmountRials > thresholdRials) {
            MerchantReceiptPrintMode.MANDATORY
        } else {
            MerchantReceiptPrintMode.OPTIONAL
        }
    }
}
