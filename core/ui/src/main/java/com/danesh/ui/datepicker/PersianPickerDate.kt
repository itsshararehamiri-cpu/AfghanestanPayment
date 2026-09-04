package com.danesh.ui.datepicker

import saman.zamani.persiandate.PersianDate

data class PersianPickerDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(month in 1..12) { "month must be between 1 and 12" }
        require(day in 1..daysInShamsiMonth(year, month)) { "invalid day for selected month" }
    }

    companion object {
        fun today(): PersianPickerDate {
            val date = PersianDate()
            return PersianPickerDate(
                year = date.shYear,
                month = date.shMonth,
                day = date.shDay,
            )
        }

        fun fromGregorianYyyyMmDd(value: String): PersianPickerDate? {
            val digits = value.filter(Char::isDigit)
            val normalized = when {
                digits.length >= 8 -> digits.take(8)
                digits.length == 6 -> "20${digits}"
                else -> return null
            }
            val year = normalized.substring(0, 4).toIntOrNull() ?: return null
            val month = normalized.substring(4, 6).toIntOrNull() ?: return null
            val day = normalized.substring(6, 8).toIntOrNull() ?: return null
            val date = PersianDate().apply {
                grgYear = year
                grgMonth = month
                grgDay = day
            }
            return PersianPickerDate(
                year = date.shYear,
                month = date.shMonth,
                day = date.shDay,
            )
        }
    }

    fun toGregorianYyyyMmDd(): String {
        val date = PersianDate().apply {
            shYear = year
            shMonth = month
            shDay = day
        }
        return "%04d%02d%02d".format(date.grgYear, date.grgMonth, date.grgDay)
    }

    fun toShamsiYyyyMmDd(): String =
        "%04d%02d%02d".format(year, month, day)

    fun displayLabel(locale: SolarCalendarLocale): String =
        "${toPersianDigits(day.toString())} ${locale.monthName(month)} ${toPersianDigits(year.toString())}"
}

fun formatGregorianFilterDateForDisplay(
    gregorianYyyyMmDd: String,
    locale: SolarCalendarLocale,
): String {
    if (gregorianYyyyMmDd.isBlank()) return ""
    return PersianPickerDate.fromGregorianYyyyMmDd(gregorianYyyyMmDd)?.displayLabel(locale)
        ?: gregorianYyyyMmDd
}

internal fun daysInShamsiMonth(year: Int, month: Int): Int {
    val date = PersianDate()
    date.shYear = year
    date.shMonth = month
    date.shDay = 1
    return date.monthLength
}

internal fun clampDay(year: Int, month: Int, day: Int): Int {
    val maxDay = daysInShamsiMonth(year, month)
    return day.coerceIn(1, maxDay)
}

fun toPersianDigits(value: String): String = buildString(value.length) {
    value.forEach { char ->
        append(
            when (char) {
                in '0'..'9' -> PERSIAN_DIGITS[char - '0']
                else -> char
            },
        )
    }
}

private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
