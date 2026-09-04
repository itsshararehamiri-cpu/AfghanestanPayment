package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R
import com.danesh.common.currency.amountWithCurrency

@Composable
fun ElectronicReceiptBalanceFeeRow(fee: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.balance_transaction_fee),
        value = amountWithCurrency(fee.formatAmount()),
        icon = com.danesh.ui.R.drawable.ic_money_send,
    )
}
