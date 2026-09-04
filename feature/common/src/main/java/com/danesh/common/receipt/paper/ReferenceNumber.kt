package com.danesh.common.receipt.paper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ReferenceNumber(  modifier: Modifier = Modifier,
                   rrn: String,
                   textColor: Color,
                   isPaperReceipt: Boolean = false) {
    ReceiptItem(
        modifier,
        first = stringResource(R.string.trace_number),
        second = rrn,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}