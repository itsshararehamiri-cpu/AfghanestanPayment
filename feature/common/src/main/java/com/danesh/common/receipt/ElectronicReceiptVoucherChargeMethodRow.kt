package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptVoucherChargeMethodRow(operatorCode: String) {
    val label = voucherChargeMethodLabel(operatorCode)
    if (label.isBlank()) return
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.charging_method),
        value = label,
        icon = R.drawable.ic_terminal_merchant,
    )
}
