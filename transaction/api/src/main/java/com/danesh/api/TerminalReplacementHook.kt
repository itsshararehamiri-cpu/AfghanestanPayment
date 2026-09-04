package com.danesh.api

/**
 * پاک‌سازی‌های اختصاصی PSP هنگام تعویض ترمینال.
 * هر flavor با Hilt [@IntoSet] هوک خود را ثبت می‌کند.
 */
fun interface TerminalReplacementHook {
    suspend fun onTerminalReplaced()
}
