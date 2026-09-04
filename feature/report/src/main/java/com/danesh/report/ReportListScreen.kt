package com.danesh.report



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.danesh.report.model.ReportFilterState

import com.danesh.report.model.ReportMenuType

import com.danesh.report.model.ReportTransactionChipFilter

import com.danesh.report.receipt.ReportTablePrintLabels

import com.danesh.report.ui.ReportAggregateScreen

import com.danesh.report.ui.ReportTransactionDetailsScreen

import com.danesh.report.ui.ReportUnsettledTransactionsScreen
import com.danesh.ui.theme.appScreenBackground



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ReportListScreen(

    reportType: ReportMenuType,

    filters: ReportFilterState,

    viewModel: ReportListViewModel,

    onBackClick: () -> Unit,

    onFilterClick: () -> Unit,onHomeClick:()-> Unit

) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var selectedChip by remember { mutableStateOf(ReportTransactionChipFilter.ALL) }

    LaunchedEffect(uiState.availableChips) {
        if (selectedChip !in uiState.availableChips) {
            selectedChip = ReportTransactionChipFilter.ALL
        }
    }

    val printLabels = ReportTablePrintLabels(

        title = when (reportType) {

            ReportMenuType.AGGREGATE -> stringResource(R.string.report_aggregate)

            else -> stringResource(R.string.report_transaction_details)

        },

        typeColumn = stringResource(R.string.report_print_column_type),

        dateColumn = stringResource(R.string.report_print_column_date),

        timeColumn = stringResource(R.string.report_print_column_time),

        amountColumn = stringResource(R.string.report_print_column_amount),

        referenceColumn = stringResource(R.string.report_print_column_reference),

        emptyMessage = stringResource(R.string.report_empty),

        printSuccessMessage = stringResource(R.string.report_print_success),

        printFailedMessage = stringResource(R.string.report_print_failed),

    )



    LaunchedEffect(filters) {
        selectedChip = ReportTransactionChipFilter.ALL

    }



    LaunchedEffect(reportType, filters, selectedChip) {
        viewModel.load(reportType, filters, selectedChip)

    }



    LaunchedEffect(uiState.printMessage) {
        if (uiState.printMessage != null) {

            kotlinx.coroutines.delay(3_000)

            viewModel.clearPrintMessage()

        }

    }

    val onAggregatePrintClick: (android.graphics.Bitmap) -> Unit = { receipt ->
        viewModel.printAggregateInvoiceReceipt(

            context = context,

            receipt = receipt,

            successMessage = printLabels.printSuccessMessage,

            failedMessage = printLabels.printFailedMessage,

        )

    }

    val onDetailPrintClick: (android.graphics.Bitmap, android.graphics.Bitmap) -> Unit =
        { sumReceipt, allReceipt ->
            viewModel.printDetailReportReceipts(

                context = context,

                sumReceipt = sumReceipt,

                allReceipt = allReceipt,

                successMessage = printLabels.printSuccessMessage,

                failedMessage = printLabels.printFailedMessage,

            )

        }



    when {

        uiState.errorMessage != null -> {
            Column(

                modifier = Modifier

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

                Text(

                    text = uiState.errorMessage.orEmpty(),

                    color = Color(0xFFFF8A80),

                    modifier = Modifier.padding(24.dp),

                    style = MaterialTheme.typography.bodyMedium,

                )

            }

        }



        reportType == ReportMenuType.AGGREGATE -> {
            ReportAggregateScreen(

                summary = uiState.summary,

                invoice = uiState.aggregateInvoice,

                onBackClick = onBackClick,

                onFilterClick = onFilterClick,

                onPrintClick = {
                    onAggregatePrintClick(it)
                },
                onRetryClick = {
                    viewModel.load(reportType, filters, selectedChip)
                },

                onHomeClick = { onHomeClick() },

                isPrinting = uiState.isPrinting,

                printMessage = uiState.printMessage,

                isLoading = uiState.isLoading,

            )

        }



        reportType == ReportMenuType.TRANSACTION_DETAILS -> {
            ReportTransactionDetailsScreen(

                title = stringResource(R.string.report_transaction_details),

                transactions = uiState.transactions,

                selectedChip = selectedChip,

                availableChips = uiState.availableChips,

                sumOfTransactions = uiState.sumOfTransactions,

                sumOfFees = uiState.sumOfFees,

                showFeeEnabled = uiState.showFeeEnabled,

                numberOfTransactions = uiState.numberOfTransactions,

                terminalId = uiState.terminalId,

                merchantId = uiState.merchantId,

                merchantPhone = uiState.merchantPhone,

                merchantName = uiState.merchantName,

                englishMerchantName = uiState.englishMerchantName,

                fromDate = uiState.fromDate,

                fromTime = uiState.fromTime,

                toDate = uiState.toDate,

                toTime = uiState.toTime,

                onChipSelected = { selectedChip = it },

                onBackClick = onBackClick,

                onFilterClick = onFilterClick,

                onPrintClick = {a,b->
                    onDetailPrintClick(a,b)
                },

                isPrinting = uiState.isPrinting,

                printMessage = uiState.printMessage,

                isLoading = uiState.isLoading,

                unsettledGroups = uiState.unsettledGroups,

                onHomeClick = { onHomeClick() },
                onRetryClick = {
                    viewModel.load(reportType, filters, selectedChip)
                },

            )

        }



        reportType == ReportMenuType.UNSETTLED -> {
            ReportUnsettledTransactionsScreen(

                transactions = uiState.transactions,

                onBackClick = onBackClick,

                isLoading = uiState.isLoading,
                onRetryClick = {
                    viewModel.load(reportType, filters, selectedChip)
                },

                modifier = Modifier.fillMaxSize(),

            )

        }



        uiState.transactions.isEmpty() -> {
            com.danesh.report.ui.ReportEmptyStateScreen(
                title = when (reportType) {
                    ReportMenuType.AGGREGATE -> stringResource(R.string.report_aggregate)
                    else -> stringResource(R.string.report_transaction_details)
                },
                message = stringResource(R.string.report_empty),
                onRetryClick = {
                    viewModel.load(reportType, filters, selectedChip)
                },
                onCancelClick = onBackClick,
            )
        }



        else -> {
            LazyColumn(

                modifier = Modifier

                    .fillMaxSize()

                    .appScreenBackground()

                    .padding(horizontal = 20.dp),

            ) {

                items(uiState.transactions) { transaction ->

                    com.danesh.report.ui.ReportTransactionListItem(transaction = transaction)

                }

            }

        }

    }

}

