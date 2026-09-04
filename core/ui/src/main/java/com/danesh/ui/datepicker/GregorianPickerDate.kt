package com.danesh.ui.datepicker

import java.util.Calendar
import java.util.GregorianCalendar

data class GregorianPickerDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(month in 1..12) { "month must be between 1 and 12" }
        require(day in 1..daysInGregorianMonth(year, month)) { "invalid day for selected month" }
    }

    companion object {
        fun today(): GregorianPickerDate {
            val calendar = Calendar.getInstance()
            return GregorianPickerDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH),
            )
        }

        fun fromYyyyMmDd(value: String): GregorianPickerDate? {
            val digits = value.filter(Char::isDigit)
            val normalized = when {
                digits.length >= 8 -> digits.take(8)
                digits.length == 6 -> "20${digits}"
                else -> return null
            }
            val year = normalized.substring(0, 4).toIntOrNull() ?: return null
            val month = normalized.substring(4, 6).toIntOrNull() ?: return null
            val day = normalized.substring(6, 8).toIntOrNull() ?: return null
            if (month !in 1..12 || day !in 1..daysInGregorianMonth(year, month)) return null
            return GregorianPickerDate(year = year, month = month, day = day)
        }
    }

    fun toYyyyMmDd(): String = "%04d%02d%02d".format(year, month, day)

    fun displayLabel(): String = toYyyyMmDd().let { raw ->
        "${raw.substring(0, 4)}/${raw.substring(4, 6)}/${raw.substring(6, 8)}"
    }
}

fun formatGregorianDateLabelForDisplay(gregorianYyyyMmDd: String): String {
    if (gregorianYyyyMmDd.isBlank()) return ""
    return GregorianPickerDate.fromYyyyMmDd(gregorianYyyyMmDd)?.displayLabel()
        ?: gregorianYyyyMmDd
}

internal fun daysInGregorianMonth(year: Int, month: Int): Int {
    val calendar = GregorianCalendar(year, month - 1, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

internal fun clampGregorianDay(year: Int, month: Int, day: Int): Int {
    val maxDay = daysInGregorianMonth(year, month)
    return day.coerceIn(1, maxDay)
}
