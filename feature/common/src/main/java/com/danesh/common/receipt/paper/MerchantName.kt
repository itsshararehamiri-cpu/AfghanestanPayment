package com.danesh.common.receipt.paper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
fun MerchantName(  modifier: Modifier = Modifier,
                   merchantName: String,
                   textColor: Color,
                   isPaperReceipt: Boolean = false) {
    ReceiptItem(
        modifier,
        first = stringResource(com.danesh.common.R.string.merchant_name),
        second = merchantName,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}