package com.danesh.api

import android.util.Log

/**
 * پس از ارسال Advice/Reverse:
 * - موفق → حذف از SAF
 * - Field 39 == "80" → نگه داشتن برای تلاش مجدد
 * - Field 39 != "80" (پاسخ واقعی سوئیچ) → حذف
 * - خطای حمل‌ونقل / بدون پاسخ → نگه داشتن
 */
object DefaultQueueRemovalPolicy {

    const val RETRY_RESPONSE_CODE = "80"

    fun shouldRemoveFromQueue(result: AdviceReverseResult): Boolean {
        if (result.isSuccess) return true

        val code = result.responseCode.trim()
        if (TransactionTransportCodes.isTransientFailure(code)) return false
        if (code == RETRY_RESPONSE_CODE) return false

        // پاسخ Field 39 از سوئیچ آمده و 80 نیست → حذف
        return code.isNotEmpty()
    }
}
