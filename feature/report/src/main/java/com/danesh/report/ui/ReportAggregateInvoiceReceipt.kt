package com.danesh.report.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.danesh.common.containerReceiptModifier
import com.danesh.common.getFontSize
import com.danesh.common.getFontWeight
import com.danesh.common.locale.localizedTitle
import com.danesh.common.receipt.AddPSPLog
import com.danesh.common.receipt.formatAmount
import com.danesh.common.rowReceiptModifier
import com.danesh.report.R
import com.danesh.report.model.AggregateInvoiceReport
import com.danesh.report.model.AggregateServiceSummary
import com.danesh.report.model.AggregateUnsettledDayGroup
import com.danesh.report.receipt.ReportReceiptDisplayFormat
import com.danesh.report.receipt.ReportSolarDateFormat

@Composable
fun ReportAggregateInvoiceReceipt(
    invoice: AggregateInvoiceReport,
) {
    val context = LocalContext.current
    val isPaperReceipt = true
    val textColor = Color.Black
    val fromDateSolar = ReportSolarDateFormat.formatOrToday(invoice.fromDate)
    val toDateSolar = ReportSolarDateFormat.formatOrToday(invoice.toDate)
    val reportDate = ReportSolarDateFormat.formatYyyyMmDd(invoice.reportDate)
        .ifBlank { toDateSolar }
    val fromTime = ReportSolarDateFormat.formatTimeHhMm(invoice.fromTime).ifBlank { "00:00" }
    val toTime = ReportSolarDateFormat.formatTimeHhMm(invoice.toTime).ifBlank { "23:59" }
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = getFontWeight(isPaperReceipt, context),
    )
    val rowModifier = Modifier.rowReceiptModifier(isPaperReceipt)
    val currency = invoice.currencyLabel.ifBlank {
        stringResource(R.string.report_aggregate_currency_rial)
    }

    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // صورتحساب تجمیعی
        Text(
            text = stringResource(R.string.report_aggregate_invoice_title),
            color = textColor,
            style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = rowModifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))

        // پایانه/زمان ]شماره پایانه[ / ]تاریخ شمسی[
        Text(
            text = stringResource(
                R.string.report_aggregate_terminal_time,
                invoice.terminalId.ifBlank { "—" },
                reportDate.ifBlank { "—" },
            ),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            modifier = rowModifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(
                R.string.report_aggregate_from_to_date,
                fromDateSolar.ifBlank { "—" },
                toDateSolar.ifBlank { "—" },
            ),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            modifier = rowModifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.report_aggregate_from_to_time,
                fromTime,
                toTime,
            ),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            modifier = rowModifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))

        // جمع کل تراکنش‌ها و تعداد کل در بازه تاریخ
        Text(
            text = stringResource(
                R.string.report_aggregate_total_count,
                invoice.successfulCount.ifBlank { "0" },
            ),
            color = textColor,
            style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            modifier = rowModifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(
                R.string.report_aggregate_total_amount,
                invoice.totalAmountFormatted.ifBlank { "0" },
                currency,
            ),
            color = textColor,
            style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            modifier = rowModifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(6.dp))

        // فقط نوع سرویس‌هایی که در بازه انتخابی تراکنش دارند.
        invoice.services.forEach { service ->
            AggregateServiceSection(
                service = service,
                currencyLabel = currency,
                textColor = textColor,
                bodyStyle = bodyStyle,
                rowModifier = rowModifier,
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // تسویه‌نشده فقط اگر در زمان دریافت گزارش موجود باشد — انتهای رسید.
        invoice.unsettledGroups.forEach { group ->
            AggregateUnsettledSection(
                group = group,
                currencyLabel = currency,
                textColor = textColor,
                bodyStyle = bodyStyle,
                rowModifier = rowModifier,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (invoice.services.isEmpty() && invoice.unsettledGroups.isEmpty()) {
            Text(
                text = stringResource(R.string.report_empty),
                color = textColor,
                style = bodyStyle,
                textAlign = TextAlign.Center,
                modifier = rowModifier.fillMaxWidth(),
            )
        }

        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = textColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
}

@Composable
private fun AggregateServiceSection(
    service: AggregateServiceSummary,
    currencyLabel: String,
    textColor: Color,
    bodyStyle: androidx.compose.ui.text.TextStyle,
    rowModifier: Modifier,
) {
    // .............................. نوع سرویس ...............................
    DottedServiceTitle(
        title = service.type.localizedTitle(),
        textColor = textColor,
        bodyStyle = bodyStyle,
    )
    // تعداد تراکنش ها: ]تعداد[
    Text(
        text = stringResource(R.string.report_aggregate_tx_count, service.count.toString()),
        color = textColor,
        style = bodyStyle,
        modifier = rowModifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
    // جمع مبلغ: ]مبلغ[ ریال
    Text(
        text = stringResource(
            R.string.report_aggregate_tx_amount,
            service.totalAmount.toString().formatAmount(),
            currencyLabel,
        ),
        color = textColor,
        style = bodyStyle,
        modifier = rowModifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
}

@Composable
internal fun ReportUnsettledGroupsReceipt(
    groups: List<AggregateUnsettledDayGroup>,
    currencyLabel: String,
) {
    if (groups.isEmpty()) return
    val context = LocalContext.current
    val isPaperReceipt = true
    val textColor = Color.Black
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = getFontWeight(isPaperReceipt, context),
    )
    val rowModifier = Modifier.rowReceiptModifier(isPaperReceipt)

    Column {
        groups.forEach { group ->
            AggregateUnsettledSection(
                group = group,
                currencyLabel = currencyLabel,
                textColor = textColor,
                bodyStyle = bodyStyle,
                rowModifier = rowModifier,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AggregateUnsettledSection(
    group: AggregateUnsettledDayGroup,
    currencyLabel: String,
    textColor: Color,
    bodyStyle: androidx.compose.ui.text.TextStyle,
    rowModifier: Modifier,
) {
    // تراکنش تسویه نشده – ]نوع سرویس[
    Text(
        text = stringResource(
            R.string.report_aggregate_unsettled_service,
            group.type.localizedTitle(),
        ),
        color = textColor,
        style = bodyStyle.copy(fontWeight = FontWeight.Bold),
        modifier = rowModifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    // مورخ ]تاریخ[
    Text(
        text = stringResource(
            R.string.report_aggregate_unsettled_dated,
            ReportSolarDateFormat.formatYyyyMmDd(group.date).ifBlank { group.date.ifBlank { "—" } },
        ),
        color = textColor,
        style = bodyStyle,
        modifier = rowModifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(2.dp))

    // ساعت | پیگیری | مبلغ به ریال
    UnsettledDataRow(
        time = stringResource(R.string.report_print_column_time),
        trace = stringResource(R.string.report_aggregate_trace_column),
        amount = stringResource(R.string.report_aggregate_amount_rial_column, currencyLabel),
        textColor = textColor,
        bodyStyle = bodyStyle.copy(fontWeight = FontWeight.Bold),
        rowModifier = rowModifier,
    )
    Text(
        text = ".".repeat(42),
        color = textColor,
        style = bodyStyle,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )

    // ]ساعت[ ]پیگیری[ ]مبلغ[
    group.rows.forEach { row ->
        UnsettledDataRow(
            time = ReportReceiptDisplayFormat.formatTime(row.time).ifBlank { row.time.ifBlank { "—" } },
            trace = row.trace.ifBlank { "—" },
            amount = row.amount.toString().formatAmount(),
            textColor = textColor,
            bodyStyle = bodyStyle,
            rowModifier = rowModifier,
        )
    }
}

@Composable
private fun UnsettledDataRow(
    time: String,
    trace: String,
    amount: String,
    textColor: Color,
    bodyStyle: androidx.compose.ui.text.TextStyle,
    rowModifier: Modifier,
) {
    Row(
        modifier = rowModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time,
            modifier = Modifier.weight(0.28f),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = trace,
            modifier = Modifier.weight(0.36f),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = amount,
            modifier = Modifier.weight(0.36f),
            color = textColor,
            style = bodyStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DottedServiceTitle(
    title: String,
    textColor: Color,
    bodyStyle: androidx.compose.ui.text.TextStyle,
) {
    // .............................. نوع سرویس ...............................
    Text(
        text = buildString {
            append(".".repeat(14))
            append(' ')
            append(title)
            append(' ')
            append(".".repeat(14))
        },
        color = textColor,
        style = bodyStyle.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    )
}
