package com.danesh.report.receipt

import android.util.Log
import androidx.compose.runtime.Composable
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.TransactionDateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportSolarDateFormat {

    @Composable
    fun formatYyyyMmDd(value: String): String {
        val digits = value.filter(Char::isDigit).take(8)
        if (digits.length != 8) return value.takeIf { it.isNotBlank() }.orEmpty()
        val context = ReceiptDateTimeContexts.current()
        return TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = digits,
            locale = context.locale,
            calendarStyle = context.calendarStyle,
        )
    }

    @Composable
    fun formatOrToday(value: String): String {
        val formatted = formatYyyyMmDd(value)
        if (formatted.isNotBlank()) return formatted
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return formatYyyyMmDd(today)
    }

    fun formatTimeHhMm(value: String): String =
        ReportReceiptDisplayFormat.formatTime(value)
}
