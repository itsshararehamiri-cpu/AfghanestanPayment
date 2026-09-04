package com.danesh.sadad.fee

import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZeroTransactionFeeCalculator @Inject constructor() : TransactionFeeCalculator {
    override fun feeForTransaction(transaction: TransactionResultDetail): Long = 0L
}
