package com.danesh.report.data

import android.util.Log
import com.danesh.api.CurrencyDefaultsProvider
import com.danesh.api.SafQueueReader
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import com.danesh.api.toTransactionResultDetail
import com.danesh.database.dao.TransactionReportDao
import com.danesh.database.entity.TransactionReportEntity
import com.danesh.report.model.AggregateInvoiceReport
import com.danesh.report.model.AggregateServiceSummary
import com.danesh.report.model.AggregateUnsettledDayGroup
import com.danesh.report.model.AggregateUnsettledRow
import com.danesh.report.model.ReportFilterState
import com.danesh.report.model.ReportSummary
import com.danesh.report.model.ReportTransactionChipFilter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: TransactionReportDao,
    private val currencyDefaults: CurrencyDefaultsProvider,
    private val safQueueReader: SafQueueReader,
    private val contextProvider: TransactionContextProvider,
) {
    suspend fun getSummary(): ReportSummary = withContext(Dispatchers.IO) {
        val todayStart = startOfTodayMillis()
        val transactions = reportDao.getAll()
            .filter { it.timestamp >= todayStart && it.isSuccessful() }
        ReportSummary(
            totalAmountToday = formatAmount(transactions.sumOf { it.amount }),
            successfulTransactionsCount = transactions.size.toString(),
            currency = currencyDefaults.currencyLabel,
        )
    }

    suspend fun getLastTransaction(): TransactionResultDetail? = withContext(Dispatchers.IO) {
        getLastSuccessfulTransactions(limit = 1).firstOrNull()
    }

    suspend fun getLastSuccessfulTransactions(limit: Int = 10): List<TransactionResultDetail> =
        withContext(Dispatchers.IO) {
            reportDao.getAll()
                .filter { it.isSuccessful() }
                .sortedNewestFirst()
                .take(limit)
                .map { it.toTransactionResultDetail() }
        }

    suspend fun getFilteredTransactions(
        filters: ReportFilterState,
        chipFilter: ReportTransactionChipFilter = ReportTransactionChipFilter.ALL,
    ): List<TransactionResultDetail> = withContext(Dispatchers.IO) {
        val normalizedFilters = filters.normalizedForQuery()
        reportDao.getAll()
            .filter { entity ->
                ReportFilterMatcher.matches(entity, normalizedFilters) &&
                    ReportFilterMatcher.matchesChip(entity, chipFilter)
            }
            .sortedNewestFirst()
            .map { it.toTransactionResultDetail() }
    }

    suspend fun getUnsettledTransactions(): List<TransactionResultDetail> = withContext(Dispatchers.IO) {
        val terminalConfig = contextProvider.getTerminalConfig()
        Log.d("TAG", "getUnsettledTransactions: jjjhjjjhjtjxei")
        safQueueReader.listAllPending()
            .map { it.toTransactionResultDetail(terminalConfig) }
    }

    fun groupUnsettledTransactions(
        transactions: List<TransactionResultDetail>,
    ): List<AggregateUnsettledDayGroup> =
        transactions
            .groupBy { it.transactionType to it.date }
            .map { (key, items) ->
                AggregateUnsettledDayGroup(
                    type = key.first,
                    date = key.second,
                    rows = items
                        .map { tx ->
                            AggregateUnsettledRow(
                                time = tx.time,
                                trace = tx.trace,
                                amount = tx.amount.filter(Char::isDigit).toLongOrNull() ?: 0L,
                            )
                        }
                        .sortedBy { it.time },
                )
            }
            .filter { it.rows.isNotEmpty() }
            .sortedWith(compareBy({ it.type.ordinal }, { it.date }))

    suspend fun getAggregateSummary(filters: ReportFilterState): ReportSummary = withContext(Dispatchers.IO) {
        val invoice = getAggregateInvoice(filters)
        ReportSummary(
            totalAmountToday = invoice.totalAmountFormatted,
            successfulTransactionsCount = invoice.successfulCount,
            currency = invoice.currencyLabel,
        )
    }
    suspend fun getAggregateInvoice(filters: ReportFilterState): AggregateInvoiceReport =
        withContext(Dispatchers.IO) {
            val successful = getFilteredTransactions(filters).filter { it.isSuccess }
            val terminalConfig = contextProvider.getTerminalConfig()
            // فقط سرویس‌های دارای تراکنش در بازه — سرویس بدون تراکنش در رسید نمی‌آید.
            val services = successful
                .groupBy { it.transactionType }
                .map { (type, items) ->
                    AggregateServiceSummary(
                        type = type,
                        count = items.size,
                        totalAmount = items.sumOf { it.amount.toLongOrNull() ?: 0L },
                    )
                }
                .filter { it.count > 0 }
                .sortedBy { it.type.ordinal }
            // فقط در صورت وجود تراکنش تسویه‌نشده در زمان دریافت گزارش، در انتهای رسید می‌آید.
            val unsettledGroups = groupUnsettledTransactions(getUnsettledTransactions())
            val totalAmount = successful.sumOf { it.amount.toLongOrNull() ?: 0L }
            val now = SimpleDateFormat("HH:mm", Locale.US).format(Date())
            AggregateInvoiceReport(
                terminalId = terminalConfig.terminalId,
                reportDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()),
                fromDate = filters.fromDate,
                toDate = filters.toDate,
                fromTime = filters.fromTime,
                toTime = filters.toTime.ifEmpty { now },
                currencyLabel = currencyDefaults.currencyLabel,
                services = services,
                unsettledGroups = unsettledGroups,
                totalAmountFormatted = formatAmount(totalAmount),
                successfulCount = successful.size.toString(),
            )
        }

    private fun TransactionReportEntity.isSuccessful(): Boolean = responseCode == 0

    private fun TransactionReportEntity.isLikelyBill(): Boolean =
        !billId.isNullOrBlank() || !payId.isNullOrBlank()

    private fun TransactionReportEntity.isLikelyTopUp(): Boolean =
        !mobileNumber.isNullOrBlank() || operatorCode != null

    private fun TransactionReportEntity.toTransactionResultDetail(): TransactionResultDetail {
        val terminalConfig = contextProvider.getTerminalConfig()
        val transactionType = with(ReportFilterMatcher) { resolveTransactionType() }
        val categoryTag = when {
            isLikelyTopUp() -> "topup"
            isLikelyBill() -> "bill"
            else -> processingCode
        }
        val maskedPanValue = maskedPan.orEmpty().maskPanForDisplay()
        return TransactionResultDetail(
            isSuccess = isSuccessful(),
            transactionType = transactionType,
            terminalId = terminalId.ifBlank { terminalConfig.terminalId },
            merchantId = merchantId.ifBlank { terminalConfig.merchantId },
            merchantName = merchantName.ifBlank { terminalConfig.merchantName },
            pan = maskedPanValue,
            maskedPan = maskedPanValue,
            trace = stan,
            rrn = rrn?.takeIf { it.isNotBlank() },
            date = dateTransaction,
            time = timeTransaction,
            dateTime = buildDateTime(dateTransaction, timeTransaction),
            responseCode = responseCode?.toString().orEmpty().padStart(2, '0'),
            responseMessage = responseMsg?.toString().orEmpty(),
            amount = amount.toString(),
            availableBalance = amount.toString(),
            merchantPhone = merchantPhone.orEmpty().ifBlank { terminalConfig.merchantPhone },
            englishMerchantName = terminalConfig.englishMerchantName,
            merchantAddress = terminalConfig.merchantAddress,
            merchantPostalCode = terminalConfig.merchantPostalCode,
            issuerName = issuer.orEmpty(),
            posCode = categoryTag,
            productCode = operatorCode?.toString().orEmpty(),
            voucherSerial = serialVoucher.orEmpty(),
            voucherPin = pinVoucher.orEmpty(),
            mobileNumber = mobileNumber.orEmpty(),
            billId = billId.orEmpty(),
            payId = payId.orEmpty(),
            destinationPan = destinationPan.orEmpty(),
            walletCode = walletCode.orEmpty(),
        )
    }

    suspend fun getLastTransactionForReprint(): TransactionResultDetail? = withContext(Dispatchers.IO) {
        val stan = contextProvider.lastSuccessfulStan().trim()
        val rrn = contextProvider.lastSuccessfulRrn().trim()
        val hasTraceReference = !isDefaultTrace(stan) || !isDefaultReference(rrn)
        val entity = if (hasTraceReference) {
            ReportReprintLookup.findEntity(
                entities = reportDao.getAll(),
                trackingNumber = stan,
                referenceNumber = rrn,
            )?.takeIf { it.isSuccessful() }
        } else {
            reportDao.getAll()
                .filter { it.isSuccessful() }
                .sortedNewestFirst()
                .firstOrNull()
        }
        entity?.toTransactionResultDetail()
    }

    suspend fun getTransactionForReprint(
        trackingNumber: String,
        referenceNumber: String,
    ): TransactionResultDetail? = withContext(Dispatchers.IO) {
        val entity = ReportReprintLookup.findEntity(
            entities = reportDao.getAll(),
            trackingNumber = trackingNumber,
            referenceNumber = referenceNumber,
        )?.takeIf { it.isSuccessful() }
        entity?.toTransactionResultDetail()
    }


    private fun isDefaultTrace(value: String): Boolean =
        value.all { it == '0' }

    private fun isDefaultReference(value: String): Boolean =
        value.all { it == '0' }

    private fun buildDateTime(date: String, time: String): String = when {
        date.isNotBlank() && time.isNotBlank() -> "$date - $time"
        date.isNotBlank() -> date
        time.isNotBlank() -> time
        else -> ""
    }

    private fun startOfTodayMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun formatAmount(amount: Long): String =
        NumberFormat.getNumberInstance(Locale.US).format(amount)
}
