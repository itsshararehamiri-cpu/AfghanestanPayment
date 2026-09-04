package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptBillIdRow(billId: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(com.danesh.common.R.string.bill_id_label),
        value = billId,
        icon = R.drawable.ic_terminal_merchant,
    )// TODO: ico 
}