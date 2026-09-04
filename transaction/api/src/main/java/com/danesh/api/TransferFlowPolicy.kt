package com.danesh.api

/**
 * سیاست انتقال وجه:
 * - HP: استعلام نام قبل از تایید + پشتیبانی کیف‌پول
 * - BP: فقط کارت‌به‌کارت مستقیم (بدون استعلام نام)
 */
interface TransferFlowPolicy {
    val requiresNameInquiry: Boolean
    val supportsWalletTransfer: Boolean
}
