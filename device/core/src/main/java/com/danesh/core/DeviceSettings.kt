package com.danesh.core

/**
 * اندیس‌های کلید روی PED. پیاده‌سازی دستگاه (مثلاً K9) هیچ مقدار پیش‌فرضی برای PSP خاصی ندارد؛
 * مقادیر از [DeviceSettingsProvider] (پیکربندی برنامه/PSP فعال) می‌آید.
 */
data class DeviceKeyIndexes(
    val masterKey: Int,
    val macKey: Int,
    val pinKey: Int,
    val dataKey: Int,
    val bootstrapMasterKey: Int = masterKey,
    val bootstrapMacKey: Int = macKey,
    val transportKey: Int = masterKey,
    /** اندیس کلید PIN که هنگام گرفتن رمز (pinblock) استفاده می‌شود. */
    val pinEntryKey: Int = pinKey,
)

/** زمان‌های انتظار سخت‌افزار (میلی‌ثانیه). */
data class DeviceTimeouts(
    val cardReadMs: Int,
    val scanMs: Int,
    val pinpadReadyMs: Long,
)

/** محدودیت ورود رمز کارت روی پین‌پد. */
data class PinEntryPolicy(
    val minLength: Int,
    val maxLength: Int,
)

data class DeviceSettings(
    val keyIndexes: DeviceKeyIndexes,
    val timeouts: DeviceTimeouts,
    val pinEntry: PinEntryPolicy,
    /** نام الگوریتم MAC در SDK دستگاه (مثلاً `TYPE_X919_00` برای ANSI X9.19 با zero-pad). */
    val macAlgorithm: String,
)

/**
 * منبع تنظیمات دستگاه. هر بار که دستگاه به مقدار نیاز دارد صدا زده می‌شود تا تغییرات زمان اجرا
 * (مثلاً اندیسی که کاربر هنگام بارگذاری کلید تعیین می‌کند) بلافاصله اعمال شود.
 */
fun interface DeviceSettingsProvider {
    fun settings(): DeviceSettings
}

/**
 * PSP می‌تواند بخشی از تنظیمات دستگاه را بازنویسی کند (مثلاً اندیس کلیدی که کاربر در
 * بارگذاری کلید انتخاب کرده). مقدار `null` یعنی پیش‌فرض برنامه برای آن PSP.
 */
interface PspDeviceSettingsOverride {
    fun keyIndexes(defaults: DeviceKeyIndexes): DeviceKeyIndexes? = null
    fun timeouts(defaults: DeviceTimeouts): DeviceTimeouts? = null
    fun pinEntry(defaults: PinEntryPolicy): PinEntryPolicy? = null
}
