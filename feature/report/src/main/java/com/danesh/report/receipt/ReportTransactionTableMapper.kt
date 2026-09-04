package com.danesh.report.receipt

import com.danesh.api.TransactionResultDetail
import com.danesh.common.locale.AppLocale
import com.danesh.common.locale.ReceiptCalendarStyle
import com.danesh.common.locale.TransactionDateTimeFormatter
import com.danesh.common.locale.titleRes
import com.danesh.common.receipt.formatAmount

fun TransactionResultDetail.toReportTableRow(
    context: android.content.Context,
    calendarStyle: ReceiptCalendarStyle,
    locale: AppLocale = AppLocale.fromTag(
        context.resources.configuration.locales[0]?.toLanguageTag(),
    ),
): ReportTableRow {
    val typeLabel = context.getString(transactionType.titleRes())
    val reference = rrn?.takeIf { it.isNotBlank() } ?: trace.ifBlank { "—" }
    val formattedAmount = amount
        .replace(",", "")
        .trim()
        .let { raw ->
            if (raw.isBlank()) "—"
            else runCatching { raw.formatAmount() }.getOrDefault(raw)
        }
    return ReportTableRow(
        type = typeLabel,
        date = formatPrintDate(date, locale, calendarStyle),
        time = formatPrintTime(time),
        amount = formattedAmount,
        reference = reference,
    )
}

private fun formatPrintDate(
    value: String,
    locale: AppLocale,
    calendarStyle: ReceiptCalendarStyle,
): String {
    val normalized = TransactionDateTimeFormatter.normalizeDateInput(value) ?: return "—"
    return TransactionDateTimeFormatter.formatDate(
        dateYyyyMmDd = normalized,
        locale = locale,
        calendarStyle = calendarStyle,
    ).ifBlank { "—" }
}

private fun formatPrintTime(value: String): String {
    return ReportReceiptDisplayFormat.formatTime(value).ifBlank { "—" }
}
