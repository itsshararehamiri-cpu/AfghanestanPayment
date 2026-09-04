package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptMerchantTerminalIdRow(terminalId: String,merchantId: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.terminal_merchant_label),
        value = "${merchantId}/${terminalId}",
        icon = R.drawable.ic_terminal_merchant,
    )
}