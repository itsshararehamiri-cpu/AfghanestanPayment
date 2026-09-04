package com.danesh.common.locale

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptNowFormatter {

    /**
     * Formats the current device time for admin/settings paper receipts.
     * English → Gregorian (Latin digits) otherwise → Shamsi (Persian digits).
     */
    fun format(
        language: AppLanguage,
        dateTimeSeparator: String = " ",
    ): String {
        val locale = language.toAppLocale()
        val calendarStyle = language.toReceiptCalendarStyle()
        val now = Date()
        val dateYyyyMmDd = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
        val timeHhMm = SimpleDateFormat("HHmm", Locale.US).format(now)
        val date = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = dateYyyyMmDd,
            locale = locale,
            calendarStyle = calendarStyle,
        )
        val time = formatTimeHm(timeHhMm, locale)
        return "$date$dateTimeSeparator$time"
    }

    fun format(localePreferences: LocalePreferences, dateTimeSeparator: String = " "): String =
        format(localePreferences.getLanguage(), dateTimeSeparator)

    private fun formatTimeHm(timeHhMm: String, locale: AppLocale): String {
        val digits = timeHhMm.filter { it.isDigit() }
        if (digits.length < 4) {
            return TransactionDateTimeFormatter.formatTime(digits, locale)
        }
        val formatted = "${digits.take(2)}:${digits.substring(2, 4)}"
        return if (locale == AppLocale.ENGLISH) {
            formatted
        } else {
            TransactionDateTimeFormatter.toPersianDigits(formatted)
        }
    }
}
