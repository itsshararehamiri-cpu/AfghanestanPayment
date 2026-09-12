package com.danesh.hp.diagnostics

/**
 * یک رکورد تشخیصی برای گواهی/پشتیبانی همراه‌پی — هرگز نباید PAN، Track 2 یا PIN block
 * از طریق این کلاس منتقل شود؛ فیلدها عمداً به شناسه‌های غیرحساس محدود شده‌اند.
 */
data class DiagnosticRecord(
    val direction: Direction,
    val mti: String,
    /** STAN و در صورت وجود RRN — هرگز PAN/Track2. */
    val correlationId: String,
    val transportOutcome: String,
    /** DE39 پاسخ؛ برای درخواست‌هایی که هنوز پاسخ ندارند خالی است. */
    val de39: String = "",
    /** فقط برای پیام‌های Reverse (1420) پر می‌شود. */
    val reversalOutcome: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
) {
    enum class Direction { OUT, IN }
}
