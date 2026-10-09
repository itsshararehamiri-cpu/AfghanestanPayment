package com.danesh.settings.domain

import java.util.Calendar
import java.util.Locale

/**
 * رمز ورود تنظیمات پشتیبان: معکوس ساعت و دقیقه فعلی دستگاه (HHmm).
 *
 * مثال: 15:24 → "1524" → "4251"
 *
 * اگر سوئیچ در LOGON رمز پشتیبان بفرستد، همان رمز جایگزین این مقدار می‌شود.
 */
object SupportAccessPassword {

    fun expectedPassword(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return "%02d%02d".format(Locale.US, hour, minute).reversed()
    }

    /**
     * اگر سوئیچ رمز پشتیبان تعیین کرده باشد ([hostPassword])، فقط همان معتبر است؛
     * وگرنه رمز پیش‌فرض (معکوس ساعت).
     */
    fun matches(
        input: String,
        calendar: Calendar = Calendar.getInstance(),
        hostPassword: String? = null,
    ): Boolean =
        if (!hostPassword.isNullOrEmpty()) input == hostPassword else input == expectedPassword(calendar)
}
