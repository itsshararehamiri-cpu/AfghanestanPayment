package com.danesh.api

/**
 * کارمزد پیش‌فرض رسید — مقدار از `BuildConfig` هر flavor PSP خوانده می‌شود.
 */
interface ReceiptFeeDefaultsProvider {
    val balanceTransactionFee: String
}
