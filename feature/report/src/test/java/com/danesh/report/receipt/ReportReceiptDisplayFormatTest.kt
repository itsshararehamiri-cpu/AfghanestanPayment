package com.danesh.report.receipt

import com.danesh.common.locale.AppLocale
import com.danesh.common.locale.ReceiptCalendarStyle
import com.danesh.common.locale.TransactionDateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportReceiptDisplayFormatTest {

    @Test
    fun formatDate_yyyyMmDd_toShamsiFormat() {
        val formatted = ReportReceiptDisplayFormat.formatDate(
            value = "20260801",
            locale = AppLocale.IRANIAN,
            calendarStyle = ReceiptCalendarStyle.IRANIAN_SHAMSI,
        )
        assertTrue(formatted.matches(Regex("۱۴۰۵/\\d{2}/\\d{2}")))
    }

    @Test
    fun formatTime_hhmmss_toHhMm() {
        assertEquals("14:35", ReportReceiptDisplayFormat.formatTime("143522"))
    }

    @Test
    fun formatTime_hhMmAlreadyFormatted() {
        assertEquals("09:05", ReportReceiptDisplayFormat.formatTime("09:05:11"))
    }
}
