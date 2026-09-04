package com.danesh.afghanestanpayment.config

import com.danesh.afghanestanpayment.BuildConfig
import com.danesh.api.ReceiptFeeDefaultsProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigReceiptFeeDefaultsProvider @Inject constructor() : ReceiptFeeDefaultsProvider {
    override val balanceTransactionFee: String = BuildConfig.DEFAULT_BALANCE_TRANSACTION_FEE
}
