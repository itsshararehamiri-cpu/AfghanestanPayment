package com.danesh.api

interface TransactionFeeCalculator {
    fun feeForTransaction(transaction: TransactionResultDetail): Long

    fun totalFeeAmount(transactions: List<TransactionResultDetail>): Long =
        transactions
            .filter { it.isSuccess }
            .sumOf { feeForTransaction(it) }
}
