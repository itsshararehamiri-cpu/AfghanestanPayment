package com.danesh.api

/**
 * سیاست UI/تراکنش پرداخت قبض:
 * - BP: مبلغ از کاربر + یک تراکنش مالی
 * - HP: بدون مبلغ ورودی + استعلام (Info) سپس پرداخت (Save)
 */
interface BillFlowPolicy {
    val requiresAmountInput: Boolean

    /**
     * همراه‌پی: کارت‌کشی (PAN) → استعلام (Info) → تایید → کارت‌کشی دوباره → پین → پرداخت (Save).
     * به‌پرداخت: false — مبلغ → کارت‌کشی → پین → یک تراکنش مالی.
     */
    val requiresInquiry: Boolean
}
