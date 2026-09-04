package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptVoucherSerialRow(voucherSerial: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(com.danesh.common.R.string.voucher_serial),
        value = voucherSerial,
        icon = R.drawable.ic_terminal_merchant,
    )// TODO: ico 
}