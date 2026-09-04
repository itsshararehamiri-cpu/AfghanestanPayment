package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptVoucherPinRow(voucherPin: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(com.danesh.common.R.string.voucher_pin),
        value = voucherPin,
        icon = R.drawable.ic_terminal_merchant,
    )// TODO: ico 
}