package com.danesh.report

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.common.locale.ReceiptCalendarStyleProvider
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.strings.AppStrings
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.report.data.ReportFeeCalculator
import com.danesh.report.data.ReportRepository
import com.danesh.report.model.AggregateInvoiceReport
import com.danesh.report.model.AggregateUnsettledDayGroup
import com.danesh.report.model.ReportFilterState
import com.danesh.report.model.ReportMenuType
import com.danesh.report.model.ReportSummary
import com.danesh.report.model.ReportTransactionChipFilter
import com.danesh.report.receipt.ReportTablePrintLabels
import com.danesh.report.receipt.ReportTransactionsTableBitmapFactory
import com.danesh.report.receipt.toReportTableRow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ReportListUiState(
    val isLoading: Boolean = true,
    val isPrinting: Boolean = false,
    val reportType: ReportMenuType = ReportMenuType.TRANSACTION_DETAILS,
    val summary: ReportSummary? = null,
    val aggregateInvoice: AggregateInvoiceReport? = null,
    val transactions: List<TransactionResultDetail> = emptyList(),
    val availableChips: List<ReportTransactionChipFilter> =
        listOf(ReportTransactionChipFilter.ALL),
    val errorMessage: String? = null,
    val printMessage: String? = null,
    val printError: String? = null,
    val unsettledNotFoundMessage: String? = null,
    val sumOfTransactions: String = "",
    val sumOfFees: String = "",
    val showFeeEnabled: Boolean = false,
    val numberOfTransactions: String = "",
    val unsettledGroups: List<AggregateUnsettledDayGroup> = emptyList(),
    val terminalId: String = "",
    val merchantId: String = "",
    val merchantPhone: String = "",
    val merchantName: String = "",
    val englishMerchantName: String = "",
    val fromDate: String = "",
    val fromTime: String = "",
    val toDate: String = "",
    val toTime: String = "",
)

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val appStrings: AppStrings,
    private val device: Device,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
    private val contextProvider: TransactionContextProvider,
    private val transactionFeeCalculator: TransactionFeeCalculator,
    private val receiptCalendarStyleProvider: ReceiptCalendarStyleProvider,
    menuFlavorFeatures: MenuFlavorFeatures,
) : ViewModel() {

    private val availableChips =
        ReportTransactionChipFilter.visibleFor(menuFlavorFeatures.enabledFeatures())

    private val _uiState = MutableStateFlow(
        ReportListUiState(availableChips = availableChips),
    )
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()

    fun load(
        reportType: ReportMenuType,
        filters: ReportFilterState,
        chipFilter: ReportTransactionChipFilter = ReportTransactionChipFilter.ALL,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, reportType = reportType, errorMessage = null)
            }
            try {
                when (reportType) {
                    ReportMenuType.AGGREGATE -> {
                        val invoice = reportRepository.getAggregateInvoice(filters)
                        val summary = ReportSummary(
                            totalAmountToday = invoice.totalAmountFormatted,
                            successfulTransactionsCount = invoice.successfulCount,
                            currency = invoice.currencyLabel,
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                summary = summary,
                                aggregateInvoice = invoice,
                                transactions = emptyList(),
                                terminalId = invoice.terminalId,
                                fromDate = filters.fromDate,
                                fromTime = filters.fromTime,
                                toDate = filters.toDate,
                                toTime = filters.toTime,
                            )
                        }
                    }

                    ReportMenuType.TRANSACTION_DETAILS -> {
                        val transactions = reportRepository.getFilteredTransactions(
                            filters = filters,
                            chipFilter = chipFilter,
                        )
                        val unsettledTransactions = reportRepository.getUnsettledTransactions()
                        val terminalConfig = contextProvider.getTerminalConfig()
                        val showFee = merchantDisplayPreferences.isShowFeeEnabled()
                        val sumOfTransactions =
                            ReportFeeCalculator.totalSuccessfulAmount(transactions)
                        val sumOfFees = if (showFee) {
                            ReportFeeCalculator.totalFeeAmount(
                                transactions = transactions,
                                feeCalculator = transactionFeeCalculator,
                            )
                        } else {
                            0L
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                transactions = transactions,
                                summary = null,
                                sumOfTransactions = sumOfTransactions.toString(),
                                sumOfFees = sumOfFees.toString(),
                                showFeeEnabled = showFee,
                                numberOfTransactions = transactions
                                    .count { tx -> tx.isSuccess }
                                    .toString(),
                                unsettledGroups = reportRepository.groupUnsettledTransactions(
                                    unsettledTransactions,
                                ),
                                terminalId = terminalConfig.terminalId,
                                merchantId = terminalConfig.merchantId,
                                merchantPhone = terminalConfig.merchantPhone,
                                merchantName = terminalConfig.merchantName,
                                englishMerchantName = terminalConfig.englishMerchantName,
                                fromDate = filters.fromDate,
                                fromTime = filters.fromTime,
                                toDate = filters.toDate,
                                toTime = filters.toTime,
                            )
                        }
                    }

                    ReportMenuType.UNSETTLED -> {
                        val transactions = reportRepository.getUnsettledTransactions()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                transactions = transactions,
                                summary = null,
                            )
                        }
                    }

                    ReportMenuType.LAST_TRANSACTION -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: appStrings.reportLoadFailed(),
                    )
                }
            }
        }
    }

    fun clearPrintMessage() {
        _uiState.update { it.copy(printMessage = null) }
    }

    fun clearPrintError() {
        _uiState.update { it.copy(printError = null) }
    }

    fun clearUnsettledNotFoundMessage() {
        _uiState.update { it.copy(unsettledNotFoundMessage = null) }
    }

    fun printUnsettledTransactions(
        context: Context,
        labels: ReportTablePrintLabels,
        notFoundMessage: String,
    ) {
        if (_uiState.value.isPrinting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPrinting = true,
                    printMessage = null,
                    printError = null,
                    unsettledNotFoundMessage = null,
                )
            }
            try {
                val transactions = reportRepository.getUnsettledTransactions()
                if (transactions.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isPrinting = false,
                            unsettledNotFoundMessage = notFoundMessage,
                        )
                    }
                    return@launch
                }
                executePrint(context, transactions, labels)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPrinting = false,
                        printError = e.message?.takeIf { msg -> msg.isNotBlank() }
                            ?: labels.printFailedMessage,
                    )
                }
            }
        }
    }

    fun printTransactions(
        context: Context,
        transactions: List<TransactionResultDetail>,
        labels: ReportTablePrintLabels,
    ) {
        if (_uiState.value.isPrinting) return
        viewModelScope.launch {
            executePrint(context, transactions, labels)
        }
    }

    fun printDetailReportReceipts(
        context: Context,
        sumReceipt: Bitmap,
        allReceipt: Bitmap,
        successMessage: String,
        failedMessage: String,
    ) {
        if (_uiState.value.isPrinting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printMessage = null, printError = null) }
            val printed = runCatching {
                printBitmap(context, sumReceipt)
                printBitmap(context, allReceipt)
            }
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    printMessage = if (printed.isSuccess) successMessage else null,
                    printError = if (printed.isSuccess) {
                        null
                    } else {
                        printed.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                            ?: failedMessage
                    },
                )
            }
        }
    }

    fun printAggregateInvoiceReceipt(
        context: Context,
        receipt: Bitmap,
        successMessage: String,
        failedMessage: String,
    ) {
        if (_uiState.value.isPrinting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true, printMessage = null, printError = null) }
            val printed = runCatching { printBitmap(context, receipt) }
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    printMessage = if (printed.isSuccess) successMessage else null,
                    printError = if (printed.isSuccess) {
                        null
                    } else {
                        printed.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                            ?: failedMessage
                    },
                )
            }
        }
    }

    fun printFilteredReport(
        context: Context,
        filters: ReportFilterState,
        chipFilter: ReportTransactionChipFilter,
        labels: ReportTablePrintLabels,
    ) {
        if (_uiState.value.isPrinting) return
        viewModelScope.launch {
            val transactions = reportRepository.getFilteredTransactions(filters, chipFilter)
            executePrint(context, transactions, labels)
        }
    }

    private suspend fun executePrint(
        context: Context,
        transactions: List<TransactionResultDetail>,
        labels: ReportTablePrintLabels,
    ) {
        _uiState.update { it.copy(isPrinting = true, printMessage = null, printError = null) }
        val rows = transactions.map {
            it.toReportTableRow(
                context = context,
                calendarStyle = receiptCalendarStyleProvider.getCalendarStyle(),
            )
        }
        val bitmap = ReportTransactionsTableBitmapFactory.create(
            title = labels.title,
            typeColumn = labels.typeColumn,
            dateColumn = labels.dateColumn,
            timeColumn = labels.timeColumn,
            amountColumn = labels.amountColumn,
            referenceColumn = labels.referenceColumn,
            rows = rows,
            emptyMessage = labels.emptyMessage,
            fonts = paperReceiptTypefaceResolver.bitmapFonts(
                context = context,
                width = 384,
                horizontalPadding = 8,
            ),
        )
        val printed = runCatching {
            printBitmap(context, bitmap)
        }
        _uiState.update {
            it.copy(
                isPrinting = false,
                printMessage = if (printed.isSuccess) labels.printSuccessMessage else null,
                printError = if (printed.isSuccess) {
                    null
                } else {
                    printed.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                        ?: labels.printFailedMessage
                },
            )
        }
    }
    fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            device.print(bitmap, context, onSuccess, onFailed)
        }
    }
    private suspend fun printBitmap(context: Context, bitmap: android.graphics.Bitmap) {

            suspendCancellableCoroutine { continuation ->
                val job = CoroutineScope(continuation.context).launch {
                    device.print(
                        bitmap = bitmap,
                        context = context,
                        onSuccess = {
                            if (continuation.isActive) continuation.resume(Unit)
                        },
                        onFailed = { message ->
                            if (continuation.isActive) {
                                continuation.resumeWith(
                                    Result.failure(IllegalStateException(message)),
                                )
                            }
                        },
                    )
                }
                continuation.invokeOnCancellation { job.cancel() }
            }

    }
}
