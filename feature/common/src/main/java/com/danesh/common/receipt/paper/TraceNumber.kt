package com.danesh.common.receipt.paper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun TraceNumber(  modifier: Modifier = Modifier,
                   trace: String,
                   textColor: Color,
                   isPaperReceipt: Boolean = false) {
    ReceiptItem(
        modifier,
        first = stringResource(R.string.trace_number),
        second = trace,
        textColor = textColor, isPaperReceipt = isPaperReceipt
    )
}