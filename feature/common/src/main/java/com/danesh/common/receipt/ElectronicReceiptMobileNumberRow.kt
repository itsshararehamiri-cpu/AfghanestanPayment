package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptMobileNumberRow(mobileNumber: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(com.danesh.common.R.string.mobile_number),
        value = mobileNumber,
        icon = R.drawable.ic_terminal_merchant,
    )// TODO: ico 
}