package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptDepositIdRow(depositId: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.deposit_id_label),
        value = depositId,
        icon = R.drawable.ic_terminal_merchant,
    )
}
