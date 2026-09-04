package com.danesh.report.data

import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportFeeCalculatorTest {

    @Test
    fun totalFeeAmount_delegatesToFeeCalculator() {
        val transactions = listOf(
            sample(amount = "10000", isSuccess = true),
            sample(amount = "20000", isSuccess = true),
            sample(amount = "50000", isSuccess = false),
        )
        val calculator = object : TransactionFeeCalculator {
            override fun feeForTransaction(transaction: TransactionResultDetail): Long =
                if (!transaction.isSuccess) 0L else 100L
        }

        val total = ReportFeeCalculator.totalFeeAmount(transactions, calculator)

        assertEquals(200L, total)
    }

    @Test
    fun totalSuccessfulAmount_ignoresFailedTransactions() {
        val transactions = listOf(
            sample(amount = "10000", isSuccess = true),
            sample(amount = "20000", isSuccess = false),
        )

        assertEquals(10000L, ReportFeeCalculator.totalSuccessfulAmount(transactions))
    }

    private fun sample(amount: String, isSuccess: Boolean) = TransactionResultDetail(
        isSuccess = isSuccess,
        transactionType = TransactionType.PURCHASE,
        amount = amount,
    )
}
