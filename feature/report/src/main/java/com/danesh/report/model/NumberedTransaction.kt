package com.danesh.report.model

import com.danesh.api.TransactionResultDetail

data class NumberedTransaction(
    val sequenceNumber: Int,
    val transaction: TransactionResultDetail,
)

fun List<TransactionResultDetail>.toNumberedTransactions(): List<NumberedTransaction> {
    val total = size
    return mapIndexed { index, transaction ->
        NumberedTransaction(
            sequenceNumber = total - index,
            transaction = transaction,
        )
    }
}
