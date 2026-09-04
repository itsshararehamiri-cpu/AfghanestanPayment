package com.danesh.afghanestanpayment.config

/**
 * پیکربندی زمان اجرای اپ بر اساس product flavor.
 *
 * [enabledFeatures] نام آیتم‌های منوی اصلی است (مثل `PURCHASE`) که در
 * `BuildConfig.ENABLED_FEATURES` برای هر PSP تعریف می‌شود.
 */
data class AppRuntimeConfig(
    val activePsp: ActivePsp,
    val activeProtocol: ActiveProtocol,
    val activeDevice: ActiveDevice,
    val enabledFeatures: Set<String>,
)
