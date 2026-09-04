package com.danesh.report.data

import com.danesh.database.entity.TransactionReportEntity

 fun List<TransactionReportEntity>.sortedNewestFirst(): List<TransactionReportEntity> =
    sortedWith(
        compareByDescending<TransactionReportEntity> { it.timestamp }
            .thenByDescending { it.transactionSortKey() }
            .thenByDescending { it.id },
    )

private fun TransactionReportEntity.transactionSortKey(): Long {
    val date = dateTransaction.filter(Char::isDigit).take(8)
    if (date.length < 8) return 0L
    val timeDigits = timeTransaction.filter(Char::isDigit)
    val time = when {
        timeDigits.length >= 6 -> timeDigits.take(6)
        timeDigits.length >= 4 -> timeDigits.take(4) + "00"
        else -> "000000"
    }
    return "$date$time".toLongOrNull() ?: 0L
}
