package com.danesh.common.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDateTimeFormatterTest {

    @Test
    fun formatDate_afghanSolar_usesIranianMonthNameForDari() {
        val formatted = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = "20260711",
            locale = AppLocale.DARI,
            calendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
        )
        assertTrue(formatted.contains("تیر") || formatted.contains("مرداد"))
        assertFalse(formatted.contains("سرطان"))
    }

    @Test
    fun formatDate_gregorian_usesMiladiFormat() {
        val formatted = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = "20260806",
            locale = AppLocale.ENGLISH,
            calendarStyle = ReceiptCalendarStyle.GREGORIAN,
        )
        assertEquals("2026/08/06", formatted)
    }

    @Test
    fun formatDate_iranianShamsi_usesNumericShamsiDate() {
        val formatted = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = "20260711",
            locale = AppLocale.IRANIAN,
            calendarStyle = ReceiptCalendarStyle.IRANIAN_SHAMSI,
        )
        assertTrue(formatted.matches(Regex("۱۴۰۵/۰۴/\\d{2}")))
    }

    @Test
    fun formatTime_omitsSeconds() {
        assertEquals("15:30", TransactionDateTimeFormatter.formatTime("153045", AppLocale.ENGLISH))
        assertEquals("09:05", TransactionDateTimeFormatter.formatTime("09:05:11", AppLocale.ENGLISH))
    }

    @Test
    fun formatDisplay_bpStyle_timeThenIranianDate() {
        val formatted = TransactionDateTimeFormatter.formatDisplay(
            dateYyyyMmDd = "20260711",
            timeHhMmSs = "153045",
            locale = AppLocale.IRANIAN,
            calendarStyle = ReceiptCalendarStyle.IRANIAN_SHAMSI,
        )
        assertTrue(formatted.matches(Regex("۱۵:۳۰ - ۱۴۰۵/۰۴/\\d{2}")))
    }

    @Test
    fun formatDisplay_hpStyle_timeThenAfghanSolarDate() {
        val formatted = TransactionDateTimeFormatter.formatDisplay(
            dateYyyyMmDd = "20260711",
            timeHhMmSs = "153045",
            locale = AppLocale.DARI,
            calendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
        )
        assertTrue(formatted.startsWith("۱۵:۳۰ - "))
    }

    @Test
    fun normalizeDateInput_supportsSixDigitDates() {
        assertEquals("20250805", TransactionDateTimeFormatter.normalizeDateInput("250805"))
    }

    @Test
    fun formatDate_alreadyShamsiDate_keepsShamsiFormat() {
        val formatted = TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = "14040805",
            locale = AppLocale.IRANIAN,
            calendarStyle = ReceiptCalendarStyle.IRANIAN_SHAMSI,
        )
        assertEquals("۱۴۰۴/۰۸/۰۵", formatted)
    }

    @Test
    fun formatDisplay_english_usesLatinDigitsAndGregorianDate() {
        val formatted = TransactionDateTimeFormatter.formatDisplay(
            dateYyyyMmDd = "20260711",
            timeHhMmSs = "153045",
            locale = AppLocale.ENGLISH,
            calendarStyle = ReceiptCalendarStyle.GREGORIAN,
        )
        assertEquals("15:30 - 2026/07/11", formatted)
    }

    @Test
    fun appLocale_fromEnUsTag_resolvesToEnglish() {
        assertEquals(AppLocale.ENGLISH, AppLocale.fromTag("en-US"))
        assertEquals(AppLocale.ENGLISH, AppLocale.fromTag("en-GB"))
    }
}
