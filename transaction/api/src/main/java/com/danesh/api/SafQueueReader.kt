package com.danesh.api

/** خواندن وضعیت صف SAF برای UI (رسید معلق، startup). */
interface SafQueueReader {
    /** اولین رکورد pending صف (چاپ‌شده یا نشده). */
    suspend fun peekFirstPending(): QueueItem?

    /** همه رکوردهای تسویه‌نشده صف SAF، از قدیمی‌ترین به جدیدترین. */
    suspend fun listAllPending(): List<QueueItem>
}
