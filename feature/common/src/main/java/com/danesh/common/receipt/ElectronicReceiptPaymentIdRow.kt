package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptPaymentIdRow(paymentId: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(com.danesh.common.R.string.payment_id_label),
        value = paymentId,
        icon = R.drawable.ic_terminal_merchant,
    )// TODO: ico 
}