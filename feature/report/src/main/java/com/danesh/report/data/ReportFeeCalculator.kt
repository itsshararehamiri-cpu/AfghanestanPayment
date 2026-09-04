package com.danesh.report.data

import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail

internal object ReportFeeCalculator {

    fun totalFeeAmount(
        transactions: List<TransactionResultDetail>,
        feeCalculator: TransactionFeeCalculator,
    ): Long = feeCalculator.totalFeeAmount(transactions)

    fun totalSuccessfulAmount(transactions: List<TransactionResultDetail>): Long =
        transactions
            .filter { it.isSuccess }
            .sumOf { tx ->
                tx.amount.filter(Char::isDigit).toLongOrNull()?.coerceAtLeast(0L) ?: 0L
            }
}
