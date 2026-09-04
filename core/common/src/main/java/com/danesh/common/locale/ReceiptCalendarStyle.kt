package com.danesh.common.locale

/** سبک نمایش تاریخ/ساعت — بسته به زبان اپ. */
enum class ReceiptCalendarStyle {
    /** پشتو — ماه‌های خورشیدی پشتو. */
    AFGHAN_SOLAR,
    /** تاریخ شمسی با نام ماه ایرانی (فروردین، …) یا عددی (۱۴۰۵/۰۴/۲۱). */
    IRANIAN_SHAMSI,
    /** میلادی — برای زبان انگلیسی. */
    GREGORIAN,
}
