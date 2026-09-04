package com.danesh.engine

import com.danesh.common.RawMessage

/**
 * همگام‌سازی ساعت دستگاه از پاسخ میزبان (مثلاً فیلد 7).
 * پیاده‌سازی PSP-specific؛ پیش‌فرض no-op.
 */
fun interface HostTimeSynchronizer {
    suspend fun syncFromResponse(response: RawMessage)
}
