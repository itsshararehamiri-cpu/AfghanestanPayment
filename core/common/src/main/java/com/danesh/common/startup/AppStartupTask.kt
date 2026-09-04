package com.danesh.common.startup

/**
 * کارهایی که پس از بالا آمدن اپ (مثلاً inject کلید تست) اجرا می‌شوند.
 * هر flavor/PSP با Hilt [@IntoSet] پیاده‌سازی خود را ثبت می‌کند.
 */
fun interface AppStartupTask {
    suspend fun run()
}
