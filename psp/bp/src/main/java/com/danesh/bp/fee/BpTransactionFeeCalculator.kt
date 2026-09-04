package com.danesh.bp.fee

import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpTransactionFeeCalculator @Inject constructor() : TransactionFeeCalculator {

    override fun feeForTransaction(transaction: TransactionResultDetail): Long {
        if (!transaction.isSuccess) return 0L
        if (transaction.transactionType != TransactionType.PURCHASE) return 0L
        if (transaction.isLikelyTopUp()) return 0L

        val amount = transaction.amount
            .filter(Char::isDigit)
            .toLongOrNull()
            ?.coerceAtLeast(0L)
            ?: 0L
        return BpPurchaseFeeRules.feeForAmount(amount)
    }

    private fun TransactionResultDetail.isLikelyTopUp(): Boolean =
        posCode.contains("topup", ignoreCase = true) ||
            merchantName.contains("شارژ", ignoreCase = true)
}
