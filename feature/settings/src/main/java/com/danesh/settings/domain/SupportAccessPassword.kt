package com.danesh.settings.domain

import java.util.Calendar
import java.util.Locale

/**
 * رمز ورود تنظیمات پشتیبان: معکوس ساعت و دقیقه فعلی دستگاه (HHmm).
 *
 * مثال: 15:24 → "1524" → "4251"
 */
object SupportAccessPassword {

    fun expectedPassword(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return "%02d%02d".format(Locale.US, hour, minute).reversed()
    }

    fun matches(input: String, calendar: Calendar = Calendar.getInstance()): Boolean =
        input == expectedPassword(calendar)
}
