package com.danesh.report.ui

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.AddMerchantNamePhone
import com.danesh.common.Dimensions
import com.danesh.common.Dimensions.PADDING_SIDE_ROW_RECEIPT
import com.danesh.common.HorizontalDivider
import com.danesh.common.RowReceipt
import com.danesh.common.containerReceiptModifier
import com.danesh.common.locale.ReceiptDateTimeContext
import com.danesh.common.locale.LocalReceiptCalendarStyle
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.TransactionDateTimeFormatter
import com.danesh.common.locale.localizedTitle
import com.danesh.common.currency.currencyLabel
import com.danesh.common.getFontSize
import com.danesh.common.getFontWeight
import com.danesh.common.receipt.AddPSPLog
import com.danesh.common.receipt.ReceiptUi
import com.danesh.common.receipt.formatAmount
import com.danesh.common.rowReceiptModifier
import com.danesh.common.rowReceiptWithPSPLogoModifier
import com.danesh.report.R
import com.danesh.report.model.AggregateInvoiceReport
import com.danesh.report.model.AggregateUnsettledDayGroup
import com.danesh.report.model.ReportSummary
import com.danesh.report.model.ReportTransactionChipFilter
import com.danesh.report.receipt.ReportReceiptDateTimeRange
import com.danesh.report.receipt.ReportReceiptDisplayFormat
import com.danesh.report.receipt.ReportSolarDateFormat
import com.danesh.report.ui.theme.ReportColors
import com.danesh.ui.button.SuccessActionButtons
import com.danesh.ui.theme.appScreenBackground

private val cardShape = RoundedCornerShape(14.dp)
private val chipShape = RoundedCornerShape(20.dp)
private val successColor = Color(0xFF00E5FF)
private val failedColor = Color(0xFFFF8A80)

@Composable
fun ReportLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = ReportColors.Accent,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
fun ReportTransactionDetailsSummaryCard(
    selectedChip: ReportTransactionChipFilter,
    numberOfTransactions: String,
    sumOfTransactions: String,
    sumOfFees: String,
    showFeeEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val currency = currencyLabel()
    val formattedSum = sumOfTransactions.ifBlank { "0" }.formatAmount()
    val formattedFee = sumOfFees.ifBlank { "0" }.formatAmount()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0XFF173A46),
                        Color(0XFF14BDF6).copy(alpha = 0.5f),
                        Color(0XFF35B7E4).copy(alpha = 0.5f),
                    ),
                ),
                shape = RoundedCornerShape(14.dp),
            )
            .background(Color(0XFF0C2C36).copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (selectedChip == ReportTransactionChipFilter.ALL) {
            ReportDetailsSummaryLine(
                text = stringResource(R.string.report_details_total_count, numberOfTransactions),
            )
            ReportDetailsSummaryLine(
                text = stringResource(R.string.report_details_total_amount, formattedSum, currency),
            )
            if (showFeeEnabled) {
                ReportDetailsSummaryLine(
                    text = stringResource(R.string.report_details_total_fee, formattedFee, currency),
                )
            }
        } else {
            Text(
                text = stringResource(R.string.report_details_chip_sum, formattedSum, currency),
                color = Color(0XFF5FFBF3),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ReportDetailsSummaryLine(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
fun ReportTransactionDetailsScreen(
    modifier: Modifier = Modifier,
    title: String,
    transactions: List<TransactionResultDetail>,
    selectedChip: ReportTransactionChipFilter,
    availableChips: List<ReportTransactionChipFilter> = ReportTransactionChipFilter.entries,
    sumOfTransactions: String = "",
    sumOfFees: String = "",
    showFeeEnabled: Boolean = false,
    numberOfTransactions: String = "",
    terminalId: String = "",
    merchantId: String = "",
    merchantPhone: String = "",
    merchantName: String = "",
    englishMerchantName: String = "",
    fromDate: String = "",
    fromTime: String = "",
    toDate: String = "",
    toTime: String = "",
    onChipSelected: (ReportTransactionChipFilter) -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onPrintClick: (sumReceipt: Bitmap, allReceipt: Bitmap) -> Unit,
    isPrinting: Boolean = false,
    printMessage: String? = null,
    isLoading: Boolean = false,
    unsettledGroups: List<AggregateUnsettledDayGroup> = emptyList(),
    onHomeClick: () -> Unit,
    onRetryClick: () -> Unit = {},
) {
    var pendingPrint by remember { mutableStateOf(false) }
    var sumReceiptBitmap: Bitmap? by remember { mutableStateOf(null) }
    var allReceiptBitmap: Bitmap? by remember { mutableStateOf(null) }

    val resolvedMerchantName = merchantName.ifBlank { englishMerchantName }.ifBlank { "-" }
    val currency = currencyLabel()
    val receiptUnsettledGroups = remember(selectedChip, unsettledGroups) {
        if (selectedChip == ReportTransactionChipFilter.ALL) unsettledGroups else emptyList()
    }

    val canBuildReceipts =
        sumOfTransactions.isNotEmpty() && numberOfTransactions.isNotEmpty()
    val receiptIdentity = remember(
        sumOfTransactions,
        sumOfFees,
        showFeeEnabled,
        numberOfTransactions,
        fromDate,
        fromTime,
        toDate,
        toTime,
        resolvedMerchantName,
        transactions,
        receiptUnsettledGroups,
        selectedChip,
    ) {
        buildString {
            append(sumOfTransactions).append('|')
            append(sumOfFees).append('|')
            append(showFeeEnabled).append('|')
            append(numberOfTransactions).append('|')
            append(fromDate).append('|')
            append(fromTime).append('|')
            append(toDate).append('|')
            append(toTime).append('|')
            append(resolvedMerchantName).append('|')
            append(selectedChip.name).append('|')
            transactions.forEach { tx ->
                append(tx.trace).append('-')
                append(tx.amount).append('-')
                append(tx.date).append('-')
                append(tx.time).append(';')
            }
            receiptUnsettledGroups.forEach { group ->
                append(group.type.name).append(':')
                append(group.date).append(':')
                group.rows.forEach { row ->
                    append(row.trace).append('-')
                    append(row.amount).append(';')
                }
            }
        }
    }

    if (transactions.isNotEmpty()) {
        ReceiptUi(
            receiptKey = "sum-$receiptIdentity",
            content = {
                ReportSum(
                    merchantName = resolvedMerchantName,
                    merchantPhone = merchantPhone,
                    englishMerchantName = englishMerchantName,
                    sumOfAllTransactions = sumOfTransactions,
                    sumOfAllFees = sumOfFees,
                    showFee = showFeeEnabled,
                    fromDate = fromDate,
                    toDate = toDate,
                    fromTime = fromTime,
                    toTime = toTime,
                    numberOfAllTransactions = numberOfTransactions,
                )
            },
        ) {
            sumReceiptBitmap = it
        }

        ReceiptUi(
            receiptKey = "all-$receiptIdentity",
            content = {
                ReportAll(
                    results = transactions,
                    selectedChip = selectedChip,
                    numberOfTransactions = numberOfTransactions,
                    sumOfTransactions = sumOfTransactions,
                    sumOfFees = sumOfFees,
                    showFee = showFeeEnabled,
                    unsettledGroups = receiptUnsettledGroups,
                    currencyLabel = currency,
                )
            },
        ) {
            allReceiptBitmap = it
        }
    }

    LaunchedEffect(pendingPrint, sumReceiptBitmap, allReceiptBitmap, isPrinting) {
        if (!pendingPrint || isPrinting) return@LaunchedEffect
        val sumBitmap = sumReceiptBitmap ?: return@LaunchedEffect
        val allBitmap = allReceiptBitmap ?: return@LaunchedEffect
        pendingPrint = false
        onPrintClick(sumBitmap, allBitmap)
    }

    val requestPrint: () -> Unit = {
        if (!isPrinting && canBuildReceipts) {
            val sumBitmap = sumReceiptBitmap
            val allBitmap = allReceiptBitmap
            if (sumBitmap != null && allBitmap != null) {
                onPrintClick(sumBitmap, allBitmap)
            } else {
                pendingPrint = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF013543),
                        Color(0XFF043746),
                        Color(0XFF002531),
                    ),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReportTransactionDetailsHeader(
                title = title,
                onBackClick = onBackClick,
                onFilterClick = onFilterClick,
                onPrintClick = requestPrint,
                isPrinting = isPrinting,
            )

            printMessage?.let { message ->
                Text(
                    text = message,
                    color = ReportColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            ReportTransactionChipRow(
                selectedChip = selectedChip,
                availableChips = availableChips,
                onChipSelected = onChipSelected,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    isLoading && transactions.isEmpty() -> Unit

                    transactions.isEmpty() -> {
                        ReportEmptyStateContent(
                            message = stringResource(R.string.report_empty),
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                ReportTransactionDetailsSummaryCard(
                                    selectedChip = selectedChip,
                                    numberOfTransactions = numberOfTransactions,
                                    sumOfTransactions = sumOfTransactions,
                                    sumOfFees = sumOfFees,
                                    showFeeEnabled = showFeeEnabled,
                                )
                            }
                            items(
                                items = transactions,
                                key = { "${it.trace}-${it.date}-${it.time}-${it.amount}" },
                            ) { transaction ->
                                ReportTransactionListItem(transaction = transaction)
                            }
                            item { Spacer(modifier = Modifier.size(24.dp)) }
                        }
                    }
                }

                ReportLoadingOverlay(visible = isLoading)
            }

            when {
                transactions.isNotEmpty() && !isLoading -> {
                    SuccessActionButtons(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        onHomeClick = onHomeClick,
                        homeEnabled = !isPrinting,
                        onPrintReceiptClick = requestPrint,
                    )
                }

                transactions.isEmpty() && !isLoading -> {
                    ReportEmptyStateActions(
                        onRetryClick = onRetryClick,
                        onCancelClick = onBackClick,
                    )
                }
            }
        }
    }
}

@Composable
fun ReportAggregateScreen(
    summary: ReportSummary?,
    invoice: AggregateInvoiceReport?,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onPrintClick: (Bitmap) -> Unit,
    onHomeClick: () -> Unit,
    isPrinting: Boolean = false,
    printMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
) {
    var pendingPrint by remember { mutableStateOf(false) }
    var receiptBitmap: Bitmap? by remember { mutableStateOf(null) }
    val receiptKey = remember(invoice) { invoice?.hashCode() }
    val isEmptyReport = invoice?.services.isNullOrEmpty() &&
        invoice?.unsettledGroups.isNullOrEmpty()
    val fromDateSolar = ReportSolarDateFormat.formatOrToday(invoice?.fromDate.orEmpty())
    val toDateSolar = ReportSolarDateFormat.formatOrToday(invoice?.toDate.orEmpty())
    val fromTime = ReportSolarDateFormat.formatTimeHhMm(invoice?.fromTime.orEmpty()).ifBlank { "00:00" }
    val toTime = ReportSolarDateFormat.formatTimeHhMm(invoice?.toTime.orEmpty()).ifBlank { "23:59" }
    val currency = invoice?.currencyLabel?.ifBlank { summary?.currency.orEmpty() }.orEmpty()
        .ifBlank { stringResource(R.string.report_aggregate_currency_rial) }

    LaunchedEffect(receiptKey) {
        receiptBitmap = null
    }

    // Same pattern as transaction-details: ReceiptUi sibling for paper bitmap.
    if (invoice != null) {
        ReceiptUi(
            receiptKey = receiptKey,
            content = { ReportAggregateInvoiceReceipt(invoice = invoice) },
        ) {
            receiptBitmap = it
        }
    }

    LaunchedEffect(pendingPrint, receiptBitmap, isPrinting) {
        if (!pendingPrint || isPrinting) return@LaunchedEffect
        val bitmap = receiptBitmap ?: return@LaunchedEffect
        pendingPrint = false
        onPrintClick(bitmap)
    }

    val requestPrint: () -> Unit = {
        if (!isPrinting && invoice != null) {
            val bitmap = receiptBitmap
            if (bitmap != null) {
                onPrintClick(bitmap)
            } else {
                pendingPrint = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF013543),
                        Color(0XFF043746),
                        Color(0XFF002531),
                    ),
                ),
            ),
    ) {
        ReportTransactionDetailsHeader(
            title = stringResource(R.string.report_aggregate_invoice_title),
            onBackClick = onBackClick,
            onFilterClick = onFilterClick,
            onPrintClick = requestPrint,
            isPrinting = isPrinting,
        )

        printMessage?.let { message ->
            Text(
                text = message,
                color = ReportColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (!isLoading && isEmptyReport) {
                ReportEmptyStateContent(
                    message = stringResource(R.string.report_empty),
                )
            } else if (summary != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.report_aggregate_from_to_date,
                                    fromDateSolar.ifBlank { "—" },
                                    toDateSolar.ifBlank { "—" },
                                ),
                                color = ReportColors.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(
                                    R.string.report_aggregate_from_to_time,
                                    fromTime,
                                    toTime,
                                ),
                                color = ReportColors.TextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    item {
                        AggregateTotalsCard(
                            totalAmount = summary.totalAmountToday,
                            totalCount = summary.successfulTransactionsCount,
                            currency = currency,
                        )
                    }

                    items(invoice?.services.orEmpty(), key = { it.type.name }) { service ->
                        AggregateServiceTypeCard(
                            title = service.type.localizedTitle(),
                            count = service.count.toString(),
                            amount = service.totalAmount.toString().formatAmount(),
                            currency = currency,
                        )
                    }
                }
            }

            ReportLoadingOverlay(visible = isLoading)
        }

        when {
            summary != null && !isLoading && !isEmptyReport -> {
                SuccessActionButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    onHomeClick = onHomeClick,
                    homeEnabled = !isPrinting,
                    onPrintReceiptClick = requestPrint,
                )
            }

            !isLoading && isEmptyReport -> {
                ReportEmptyStateActions(
                    onRetryClick = onRetryClick,
                    onCancelClick = onBackClick,
                )
            }
        }
    }
}

@Composable
private fun AggregateTotalsCard(
    totalAmount: String,
    totalCount: String,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0XFF173A46),
                        Color(0XFF14BDF6).copy(0.5f),
                        Color(0XFF35B7E4).copy(0.5f),
                    ),
                ),
                RoundedCornerShape(16.dp),
            )
            .background(Color(0XFF0C2C36).copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.total_sum),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.number_of_all_transactions),
                color = ReportColors.TextPrimary,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = totalCount,
                color = Color(0XFF5FFBF3),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.sum_of_all_transactions),
                color = ReportColors.TextPrimary,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "$totalAmount $currency".trim(),
                color = Color(0XFF5FFBF3),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun AggregateServiceTypeCard(
    title: String,
    count: String,
    amount: String,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0XFF0C2C36).copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.report_aggregate_tx_count, count),
                color = ReportColors.TextPrimary,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.report_aggregate_tx_amount, amount, currency),
                color = Color(0XFF5FFBF3),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ReportUnsettledTransactionsScreen(
    transactions: List<TransactionResultDetail>,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
) {
    val unsettledColor = Color(0xFFFFB74D)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0XFF013543),
                        Color(0XFF043746),
                        Color(0XFF002531),
                    ),
                ),
            ),
    ) {
        ReportTransactionDetailsHeader(
            title = stringResource(R.string.report_unsettled),
            onBackClick = onBackClick,
            onFilterClick = null,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                isLoading && transactions.isEmpty() -> Unit

                transactions.isEmpty() -> {
                    ReportEmptyStateContent(
                        message = stringResource(R.string.report_unsettled_empty),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = transactions,
                            key = { "${it.trace}-${it.date}-${it.time}-${it.amount}" },
                        ) { transaction ->
                            ReportTransactionListItem(
                                transaction = transaction,
                                statusLabelRes = R.string.report_status_unsettled,
                                statusColorOverride = unsettledColor,
                            )
                        }
                        item { Spacer(modifier = Modifier.size(24.dp)) }
                    }
                }
            }

            ReportLoadingOverlay(visible = isLoading)
        }

        if (transactions.isEmpty() && !isLoading) {
            ReportEmptyStateActions(
                onRetryClick = onRetryClick,
                onCancelClick = onBackClick,
            )
        }
    }
}

@Composable
fun ReportTransactionDetailsHeader(    modifier: Modifier = Modifier,

                                       title: String,
    onBackClick: () -> Unit,
    onFilterClick: (() -> Unit)?,
    onPrintClick: (() -> Unit)? = null,
    isPrinting: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(com.danesh.common.R.drawable.ic_white_arrow_to_right),
                contentDescription = stringResource(R.string.report_back),
                tint = ReportColors.TextPrimary,
            )
        }

        Text(
            text = title,
            color = ReportColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )

        if (onFilterClick != null) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter_settings),
                    contentDescription = stringResource(R.string.report_filters_title),
                    tint = ReportColors.TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        if (onPrintClick != null) {
            if (isPrinting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(22.dp),
                    strokeWidth = 2.dp,
                    color = ReportColors.Accent,
                )
            } else {
                IconButton(onClick = onPrintClick) {
                    Icon(
                        painter = painterResource(com.danesh.ui.R.drawable.ic_printer),
                        contentDescription = stringResource(R.string.report_print),
                        tint = ReportColors.TextPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        } else if (onFilterClick == null) {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun ReportTransactionChipRow(
    selectedChip: ReportTransactionChipFilter,
    onChipSelected: (ReportTransactionChipFilter) -> Unit,
    modifier: Modifier = Modifier,
    availableChips: List<ReportTransactionChipFilter> = ReportTransactionChipFilter.entries,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        availableChips.forEach { chip ->
            ReportTransactionChip(
                labelRes = chip.labelRes,
                selected = chip == selectedChip,
                onClick = { onChipSelected(chip) },
            )
        }
    }
}

@Composable
private fun ReportTransactionChip(
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) successColor else ReportColors.FieldBorder
    val textColor = if (selected) successColor else ReportColors.TextPrimary

    Box(
        modifier = Modifier
            .clip(chipShape)
            .border(1.dp, borderColor, chipShape)
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(labelRes),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ReportTransactionListItem(
    transaction: TransactionResultDetail,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    sequenceNumber: Int? = null,
    @StringRes statusLabelRes: Int? = null,
    statusColorOverride: Color? = null,
) {
    val statusColor = statusColorOverride
        ?: if (transaction.isSuccess) Color(0XFF00FFD4) else failedColor
    val dateTimeContext = ReceiptDateTimeContexts.current()
    val typeTitle = transaction.transactionType.localizedTitle()
    val statusTitle = statusLabelRes?.let { stringResource(it) }
        ?: stringResource(
            if (transaction.isSuccess) R.string.report_status_success else R.string.report_status_failed,
        )
    val dateTimeLabel = formatListDateTime(transaction, dateTimeContext)
    val amountLabel = buildAmountLabel(transaction.amount, currencyLabel())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF0C2C36).copy(alpha = 0.5f))
            .border(
                1.dp, brush = Brush.linearGradient(
                    listOf(
                        Color(0XFF173A46),
                        Color(0XFF14BD46).copy(alpha = 0.5f),
                        Color(0XFF35B7E4).copy(alpha = 0.5f)
                    )
                ), cardShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (sequenceNumber != null) {
            ReportTransactionSequenceBadge(sequenceNumber = sequenceNumber)
        } else {
            ReportTransactionTypeIcon(transactionType = transaction.transactionType)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "$typeTitle-$statusTitle",
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = dateTimeLabel,
                color = Color(0XFF00FFD4),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = amountLabel,
                color = Color(0XFF00FFD4),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ReportColors.TextPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ReportTransactionSequenceBadge(
    sequenceNumber: Int,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A1F28))
            .border(1.dp, ReportColors.FieldBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sequenceNumber.toString(),
            color = Color(0XFF00FFD4),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReportTransactionTypeIcon(
    transactionType: TransactionType,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A1F28))
            .border(1.dp, ReportColors.FieldBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(transactionType.iconRes()),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}

@DrawableRes
private fun TransactionType.iconRes(): Int = when (this) {
    TransactionType.BALANCE -> R.drawable.ic_purchase
    TransactionType.PURCHASE ->R.drawable.ic_purchase
    TransactionType.CARD_TO_CARD -> R.drawable.ic_card_to_card
    TransactionType.CARD_TO_WALLET -> R.drawable.ic_card_to_card
    TransactionType.WALLET_TO_WALLET -> R.drawable.ic_card_to_card
    TransactionType.BILL ->R.drawable.ic_bill
    TransactionType.CASH_DEPOSIT -> R.drawable.ic_cash_deposit
    TransactionType.CASH_OUT -> R.drawable.ic_cash_out
    else ->R.drawable.ic_purchase
}


private fun formatListDateTime(
    transaction: TransactionResultDetail,
    context: ReceiptDateTimeContext,
): String {
    val date = transaction.date
    val time = transaction.time
    if (date.isBlank() && time.isBlank()) {
        return transaction.dateTime
    }
    return TransactionDateTimeFormatter.formatDisplay(
        dateYyyyMmDd = date,
        timeHhMmSs = time,
        locale = context.locale,
        calendarStyle = context.calendarStyle,
    )
}

private fun buildAmountLabel(amount: String, currencyLabel: String): String {
    if (amount.isBlank()) return "— $currencyLabel"
    val normalized = amount.replace(",", "").trim()
    val formatted = runCatching { normalized.formatAmount() }.getOrDefault(normalized)
    return "$formatted $currencyLabel"
}
@Composable
private fun ReportAll(
    results: List<TransactionResultDetail>,
    selectedChip: ReportTransactionChipFilter,
    numberOfTransactions: String,
    sumOfTransactions: String,
    sumOfFees: String = "",
    showFee: Boolean = false,
    unsettledGroups: List<AggregateUnsettledDayGroup> = emptyList(),
    currencyLabel: String = "",
) {
    val context = LocalContext.current
    val isPaperReceipt = true
    val firstColor = Black
    val resolvedCurrency = currencyLabel.ifBlank { stringResource(R.string.report_aggregate_currency_rial) }
    Column(
        modifier = Modifier.containerReceiptModifier(isPaperReceipt, context),
    ) {
        ReportTransactionsGridTable(
            results = results,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        ReportDetailsSummaryReceipt(
            selectedChip = selectedChip,
            numberOfTransactions = numberOfTransactions,
            sumOfTransactions = sumOfTransactions,
            sumOfFees = sumOfFees,
            showFee = showFee,
            currencyLabel = resolvedCurrency,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        ReportUnsettledGroupsReceipt(
            groups = unsettledGroups,
            currencyLabel = resolvedCurrency,
        )
        AddPSPLog(
            modifier = Modifier.fillMaxWidth(),
            color = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
    }
}

@Composable
private fun ReportDetailsSummaryReceipt(
    selectedChip: ReportTransactionChipFilter,
    numberOfTransactions: String,
    sumOfTransactions: String,
    sumOfFees: String,
    showFee: Boolean,
    currencyLabel: String,
    textColor: Color,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val rowModifier = Modifier.rowReceiptModifier(isPaperReceipt).padding(top = 4.dp)
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = getFontWeight(isPaperReceipt, context),
    )
    val formattedSum = sumOfTransactions.ifBlank { "0" }.formatAmount()
    val formattedFee = sumOfFees.ifBlank { "0" }.formatAmount()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (selectedChip == ReportTransactionChipFilter.ALL) {
            Text(
                text = stringResource(R.string.report_details_total_count, numberOfTransactions),
                modifier = rowModifier.fillMaxWidth(),
                color = textColor,
                style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.report_details_total_amount, formattedSum, currencyLabel),
                modifier = rowModifier.fillMaxWidth(),
                color = textColor,
                style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            )
            if (showFee) {
                Text(
                    text = stringResource(R.string.report_details_total_fee, formattedFee, currencyLabel),
                    modifier = rowModifier.fillMaxWidth(),
                    color = textColor,
                    style = bodyStyle.copy(fontWeight = FontWeight.Bold),
                )
            }
        } else {
            Text(
                text = stringResource(R.string.report_details_chip_sum, formattedSum, currencyLabel),
                modifier = rowModifier.fillMaxWidth(),
                color = textColor,
                style = bodyStyle.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun ReportTransactionsGridTable(
    results: List<TransactionResultDetail>,
    textColor: Color,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val cellStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = getFontWeight(isPaperReceipt, context),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, textColor)
            .background(White),
    ) {
        ReportGridRow(
            cells = listOf(
                stringResource(R.string.row1),
                stringResource(R.string.report_print_column_type),
                stringResource(R.string.report_print_column_date),
                stringResource(R.string.report_print_column_time),
                stringResource(R.string.report_print_column_amount),
                stringResource(R.string.report_print_column_reference),
            ),
            weights = reportTableColumnWeights,
            textColor = textColor,
            textStyle = cellStyle.copy(fontWeight = FontWeight.Bold),
            isHeader = true,
        )
        results.forEachIndexed { index, transaction ->
            ReportTableHorizontalLine(color = textColor)
            ReportGridRow(
                cells = listOf(
                    (index + 1).toString(),
                    transaction.transactionType.localizedTitle(),
                    ReportSolarDateFormat.formatYyyyMmDd(transaction.date)
                        .ifBlank { transaction.date },
                    ReportReceiptDisplayFormat.formatTime(transaction.time)
                        .ifBlank { transaction.time },
                    transaction.amount.formatAmount(),
                    transaction.rrn?.takeIf { it.isNotBlank() }
                        ?: transaction.trace.ifBlank { "—" },
                ),
                weights = reportTableColumnWeights,
                textColor = textColor,
                textStyle = cellStyle,
                isHeader = false,
            )
        }
    }
}

private val reportTableColumnWeights = floatArrayOf(0.10f, 0.18f, 0.20f, 0.12f, 0.20f, 0.20f)

@Composable
private fun ReportRangeGridTable(
    fromDateLabel: String,
    toDateLabel: String,
    fromTimeLabel: String,
    toTimeLabel: String,
    range: ReportReceiptDateTimeRange,
    textColor: Color,
    isPaperReceipt: Boolean,
) {
    val context = LocalContext.current
    val cellStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = getFontSize(isPaperReceipt, context),
        fontWeight = getFontWeight(isPaperReceipt, context),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, textColor)
            .background(White),
    ) {
        ReportGridRow(
            cells = listOf(fromDateLabel, range.fromDate, toDateLabel, range.toDate),
            weights = reportRangeColumnWeights,
            textColor = textColor,
            textStyle = cellStyle,
            isHeader = false,
        )
        ReportTableHorizontalLine(color = textColor)
        ReportGridRow(
            cells = listOf(fromTimeLabel, range.fromTime, toTimeLabel, range.toTime),
            weights = reportRangeColumnWeights,
            textColor = textColor,
            textStyle = cellStyle,
            isHeader = false,
        )
    }
}

@Composable
private fun ReportTableHorizontalLine(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

private val reportRangeColumnWeights = floatArrayOf(0.22f, 0.28f, 0.22f, 0.28f)

@Composable
private fun ReportGridRow(
    cells: List<String>,
    weights: FloatArray,
    textColor: Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    isHeader: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFF2F2F2) else White),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(textColor),
                )
            }
            Text(
                text = cell,
                modifier = Modifier
                    .weight(weights.getOrElse(index) { 1f })
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                color = textColor,
                style = textStyle,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
@Composable
fun ReportSum(
    merchantName: String,
    merchantPhone: String,
    englishMerchantName: String,
    fromDate: String,
    fromTime: String,
    toDate: String,
    toTime: String,
    numberOfAllTransactions: String,
    sumOfAllTransactions: String,
    sumOfAllFees: String = "",
    showFee: Boolean = false,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .containerReceiptModifier(true, context)
    ) {
        val isPaperReceipt = true
        val firstColor = Color.Black
        val modifierRowReceipt = Modifier.rowReceiptModifier(true)
        if (!isPaperReceipt) {
            AddPSPLog(
                modifier = Modifier.fillMaxWidth(),
                color = firstColor,
                isPaperReceipt = isPaperReceipt
            )
        }
        Row(
            modifier = modifierRowReceipt
                .fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_sum),
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = if (isPaperReceipt) 0.dp else PADDING_SIDE_ROW_RECEIPT),
                color = firstColor,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = getFontSize(isPaperReceipt, context),
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    ),
                textAlign = TextAlign.Center
            )

        }
        AddMerchantNamePhone(
            modifier = Modifier.rowReceiptWithPSPLogoModifier(isPaperReceipt),
            merchantName = merchantName,
            merchantPhone = merchantPhone,
            englishMerchantName = englishMerchantName,
            textColor = firstColor, isPaperReceipt = isPaperReceipt
        )
        val range = ReportReceiptDisplayFormat.resolveRange(
            context = context,
            fromDate = fromDate,
            toDate = toDate,
            fromTime = fromTime,
            toTime = toTime,
            calendarStyle = LocalReceiptCalendarStyle.current,
        )
        ReportRangeGridTable(
            fromDateLabel = stringResource(R.string.from_date),
            toDateLabel = stringResource(R.string.to_date),
            fromTimeLabel = stringResource(R.string.from_time),
            toTimeLabel = stringResource(R.string.to_time),
            range = range,
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        HorizontalDivider(
            modifierRowReceipt.padding(top = 3.dp),
            isPaperReceipt = isPaperReceipt
        )
        ReportDetailsSummaryReceipt(
            selectedChip = ReportTransactionChipFilter.ALL,
            numberOfTransactions = numberOfAllTransactions,
            sumOfTransactions = sumOfAllTransactions,
            sumOfFees = sumOfAllFees,
            showFee = showFee,
            currencyLabel = currencyLabel(),
            textColor = firstColor,
            isPaperReceipt = isPaperReceipt,
        )
        if (isPaperReceipt) {
            AddPSPLog(
                modifier = Modifier.fillMaxWidth(),
                color = firstColor,
                isPaperReceipt = isPaperReceipt,
            )
        }
    }
}

@Composable
fun AddNumberOfAllTransactions(
    modifier: Modifier = Modifier,
    number: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier,
        first = stringResource(R.string.number_of_all_transactions),
        second = number,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}
@Composable
fun AddSumOfAllTransactions(
    modifier: Modifier = Modifier,
    sum: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    RowReceipt(
        modifier,
        first = stringResource(R.string.sum_of_all_transactions),
        second = "${currencyLabel()} ${sum.formatAmount()}",
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}

@Composable
fun AddSumOfAllFees(
    modifier: Modifier = Modifier,
    sum: String,
    textColor: Color,
    isPaperReceipt: Boolean = false,
) {
    RowReceipt(
        modifier,
        first = stringResource(R.string.sum_of_all_fees),
        second = "${currencyLabel()} ${sum.ifBlank { "0" }.formatAmount()}",
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
    )
}

@Composable
fun DateHeader(dateTransaction: String, isPaperReceipt: Boolean = false) {
    Text(
        text = ReportSolarDateFormat.formatYyyyMmDd(dateTransaction).ifBlank { dateTransaction },
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = if (isPaperReceipt) Dimensions.FONT_SIZE_PAPER_RECEIPT else
                Dimensions.FONT_SIZE_RECEIPT,
            fontWeight = if (isPaperReceipt) FontWeight.Medium else FontWeight.Normal
        ),
        modifier = Modifier
            .padding(
                top = 3.dp,
                start = if (isPaperReceipt) 3.dp else 7.dp,
                end = if (isPaperReceipt) 3.dp else 7.dp
            )
            .fillMaxWidth()
            .padding(top = 3.dp),
        color = if (isPaperReceipt) Black else MaterialTheme.colorScheme.primary
    )
}

@Composable
fun TransactionRow(
    transaction: TransactionResultDetail, index: Int, textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context= LocalContext.current
    ConstraintLayout(
        ConstraintSet {
            val row1 = createRefFor("row1")
            val time = createRefFor("time")
            val trace = createRefFor("trace")
            val amount = createRefFor("amount")
            val type = createRefFor("type")
            val divider = createRefFor("divider")
            constrain(row1) {
                top.linkTo(parent.top, 5.dp)
                start.linkTo(parent.start)
                width = Dimension.percent(0.15f)
            }
            constrain(time) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                start.linkTo(row1.end, 5.dp)
                width = Dimension.percent(0.15f)
            }
            constrain(trace) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                start.linkTo(time.end, 5.dp)
                width = Dimension.percent(0.2f)

            }
            constrain(type) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                end.linkTo(parent.end)
                width = Dimension.percent(0.25f)
            }
            constrain(amount) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                end.linkTo(type.start, 5.dp)
                width = Dimension.percent(0.25f)
            }
            constrain(divider) {
                top.linkTo(row1.bottom, 5.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        }, modifier = Modifier
            .padding(horizontal = if (isPaperReceipt) 0.dp else 5.dp)
            .fillMaxWidth(1f)
    ) {

        Text(
            text = transaction.transactionType.localizedTitle(),
            modifier = Modifier.layoutId("type"),
            color = textColor, style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )

        Text(
            text = transaction.amount.formatAmount(), modifier = Modifier.layoutId("amount"),
            color = textColor, style = MaterialTheme.typography.titleSmall
                .copy(
                    fontSize =  getFontSize(isPaperReceipt,context),
                    fontWeight = getFontWeight(isPaperReceipt,context)
                ), textAlign = TextAlign.Center
        )

        Text(
            text = transaction.trace,
            modifier = Modifier.layoutId("trace"),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )
        Text(
            text = ReportReceiptDisplayFormat.formatTime(transaction.time)
                .ifBlank { transaction.time },
            modifier = Modifier.layoutId("time"),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )

        Text(
            text = index.toString(), modifier = Modifier
                .padding(start = 5.dp)
                .layoutId("row1"),
            color = textColor, style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(textColor)
                .layoutId("divider")
        )
    }
}


@Composable
fun HeaderRow(
    modifier: Modifier,
    titles: List<String>,
    textColor: androidx.compose.ui.graphics.Color,
    isPaperReceipt: Boolean = false
) {
    val context= LocalContext.current
    ConstraintLayout(
        ConstraintSet {
            val row1 = createRefFor("row1")
            val time = createRefFor("time")
            val trace = createRefFor("trace")
            val amount = createRefFor("amount")
            val type = createRefFor("type")
            val divider = createRefFor("divider")
            constrain(row1) {
                top.linkTo(parent.top, if (isPaperReceipt) 2.dp else 5.dp)
                start.linkTo(parent.start)
                width = Dimension.percent(0.15f)
            }
            constrain(time) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                start.linkTo(row1.end, if (isPaperReceipt) 2.dp else 5.dp)
                width = Dimension.percent(0.15f)
            }
            constrain(trace) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                start.linkTo(time.end, if (isPaperReceipt) 2.dp else 5.dp)
                width = Dimension.percent(0.2f)
            }
            constrain(type) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                end.linkTo(parent.end)
                start.linkTo(amount.end)
                width = Dimension.percent(0.24f)
            }
            constrain(amount) {
                top.linkTo(row1.top)
                bottom.linkTo(row1.bottom)
                start.linkTo(trace.end)
                end.linkTo(type.start, if (isPaperReceipt) 2.dp else 5.dp)
                width = Dimension.percent(0.26f)
            }
            constrain(divider) {
                top.linkTo(row1.bottom, if (isPaperReceipt) 2.dp else 5.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        }, modifier = modifier
            .padding(horizontal = if (isPaperReceipt) 0.dp else 12.dp)
            .fillMaxWidth(if (isPaperReceipt) 0.5f else 1f)
            .border(
                1.dp,
                if (isPaperReceipt) Black else MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp
                )
            )
            .background(
                color = if (isPaperReceipt) White else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            )
    ) {

        Text(
            text = titles[0],
            modifier = Modifier
                .padding(end = 5.dp)
                .layoutId("type")
                .padding(top = 5.dp, bottom = 5.dp),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = titles[1],
            modifier = Modifier
                .layoutId("amount")
                .padding(top = 5.dp, bottom = 5.dp),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context),
                textAlign = TextAlign.Center
            )
        )

        Text(
            text = titles[2],
            modifier = Modifier
                .layoutId("trace")
                .padding(top = 5.dp, bottom = 5.dp),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context),
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = titles[3],
            modifier = Modifier
                .layoutId("time")
                .padding(top = 5.dp, bottom = 5.dp),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )

        Text(
            text = titles[4],
            modifier = Modifier
                .padding(start = 5.dp)
                .layoutId("row1")
                .padding(top = 5.dp, bottom = 5.dp),
            color = textColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize =  getFontSize(isPaperReceipt,context),
                fontWeight = getFontWeight(isPaperReceipt,context)
            ), textAlign = TextAlign.Center
        )
    }
}

