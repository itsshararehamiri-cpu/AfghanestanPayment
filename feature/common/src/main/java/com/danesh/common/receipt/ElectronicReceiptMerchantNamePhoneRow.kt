package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R
import com.danesh.common.locale.displayMerchantName

@Composable
fun ElectronicReceiptMerchantNamePhoneRow(
    merchantName: String,
    merchantPhone: String,
    englishMerchantName: String = "",
) {
    val displayName = truncateMerchantNameForReceipt(
        name = displayMerchantName(merchantName, englishMerchantName),
        isPaperReceipt = false,
    )
    ElectronicReceiptDetailRow(
        label = displayName,
        value = merchantPhone,
        icon = com.danesh.common.R.drawable.ic_terminal_merchant,
    )
}
