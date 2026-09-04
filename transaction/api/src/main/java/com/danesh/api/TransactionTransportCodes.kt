package com.danesh.api

/**
 * کدهای خطای لایهٔ حمل‌ونقل — مقادیر عددی منفی (جدا از Field 39 سوئیچ).
 */
object TransactionTransportCodes {
    /** خطای عمومی شبکه / استثناء */
    const val NETWORK_ERROR = "-1"

    /** timeout یا خطای دریافت */
    const val RECEIVE_FAILED = "-2"

    /** اتصال به host برقرار نشد */
    const val CONNECT_FAILED = "-3"

    /** ارسال پیام ISO ناموفق بود */
    const val SEND_FAILED = "-4"

    /** صف SAF هنوز خالی نشده و تراکنش جدید مسدود شده */
    const val QUEUE_BLOCKED = "-6"

    private val legacyAliases = mapOf(
        "QUEUE" to QUEUE_BLOCKED,
        "QUEUE_BLOCKED" to QUEUE_BLOCKED,
        "NET" to RECEIVE_FAILED,
        "RECEIVE" to RECEIVE_FAILED,
        "RECEIVE_FAILED" to RECEIVE_FAILED,
        "CONN" to CONNECT_FAILED,
        "CONNECT" to CONNECT_FAILED,
        "CONNECT_FAILED" to CONNECT_FAILED,
        "SEND" to SEND_FAILED,
        "SEND_FAILED" to SEND_FAILED,
        "NETWORK_ERROR" to NETWORK_ERROR,
    )

    fun normalizeCode(code: String?): String {
        val trimmed = code?.trim().orEmpty()
        if (trimmed.isEmpty()) return trimmed
        legacyAliases[trimmed.uppercase()]?.let { return it }
        return trimmed
    }

    fun isTransportCode(code: String?): Boolean =
        normalizeCode(code) in transportCodes

    private val transportCodes = setOf(
        NETWORK_ERROR,
        RECEIVE_FAILED,
        CONNECT_FAILED,
        SEND_FAILED,
        QUEUE_BLOCKED,
    )

    /**
     * خطاهای موقت — پاسخ Field 39 از سوئیچ نیامده؛ رکورد SAF نگه داشته می‌شود.
     */
    fun isTransientFailure(code: String?): Boolean {
        if (normalizeCode(code) in transportCodes) return true
        return normalizeCode(code).isEmpty()
    }
}
