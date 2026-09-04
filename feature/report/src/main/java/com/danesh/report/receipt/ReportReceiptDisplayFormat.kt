package com.danesh.report.receipt

import android.content.Context
import com.danesh.common.locale.AppLocale
import com.danesh.common.locale.ReceiptCalendarStyle
import com.danesh.common.locale.TransactionDateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportReceiptDateTimeRange(
    val fromDate: String,
    val toDate: String,
    val fromTime: String,
    val toTime: String,
)

object ReportReceiptDisplayFormat {

    private const val DEFAULT_FROM_TIME = "00:00"
    private const val DEFAULT_TO_TIME = "23:59"

    fun formatDate(
        value: String,
        locale: AppLocale = AppLocale.default,
        calendarStyle: ReceiptCalendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
    ): String {
        val normalized = TransactionDateTimeFormatter.normalizeDateInput(value) ?: return ""
        return TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = normalized,
            locale = locale,
            calendarStyle = calendarStyle,
        )
    }

    /** Hour:minute only (`HH:mm`). */
    fun formatTime(value: String): String {
        val digits = value.filter(Char::isDigit)
        return when {
            digits.length >= 4 ->
                "${digits.substring(0, 2)}:${digits.substring(2, 4)}"
            value.contains(':') -> {
                val parts = value.split(':')
                if (parts.size >= 2) {
                    val hour = parts[0].filter(Char::isDigit).padStart(2, '0').takeLast(2)
                    val minute = parts[1].filter(Char::isDigit).padStart(2, '0').take(2)
                    "$hour:$minute"
                } else {
                    value
                }
            }
            value.isNotBlank() -> value
            else -> ""
        }
    }

    fun resolveRange(
        context: Context,
        fromDate: String,
        toDate: String,
        fromTime: String,
        toTime: String,
        now: Date = Date(),
        calendarStyle: ReceiptCalendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
    ): ReportReceiptDateTimeRange {
        val locale = AppLocale.fromTag(context.resources.configuration.locales[0]?.toLanguageTag())
        val todayGregorian = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
        val resolvedFromDate = fromDate.trim().takeIf { it.isNotEmpty() }
            ?.let { formatDate(it, locale, calendarStyle) }
            ?.takeIf { it.isNotEmpty() }
            ?: formatDate(
                deviceStartUsageGregorianDate(context),
                locale,
                calendarStyle,
            )
        val resolvedToDate = toDate.trim().takeIf { it.isNotEmpty() }
            ?.let { formatDate(it, locale, calendarStyle) }
            ?.takeIf { it.isNotEmpty() }
            ?: formatDate(todayGregorian, locale, calendarStyle)
        val resolvedFromTime = fromTime.trim().takeIf { it.isNotEmpty() }
            ?.let(::formatTime)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_FROM_TIME
        val resolvedToTime = toTime.trim().takeIf { it.isNotEmpty() }
            ?.let(::formatTime)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_TO_TIME
        return ReportReceiptDateTimeRange(
            fromDate = resolvedFromDate,
            toDate = resolvedToDate,
            fromTime = resolvedFromTime,
            toTime = resolvedToTime,
        )
    }

    private fun deviceStartUsageGregorianDate(context: Context): String {
        val millis = deviceStartUsageMillis(context)
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(millis))
    }

    private fun deviceStartUsageMillis(context: Context): Long {
        return runCatching {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.firstInstallTime
        }.getOrDefault(System.currentTimeMillis())
    }
}
