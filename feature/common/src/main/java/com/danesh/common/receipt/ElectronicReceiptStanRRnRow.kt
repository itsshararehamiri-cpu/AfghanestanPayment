package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptStanRRnRow(stan: String, rrn: String?) {
    val displayValue = formatStanRrnDisplay(stan, rrn)
    if (displayValue.isBlank()) return

    ElectronicReceiptDetailRow(
        label = stringResource(
            if (rrn.isNullOrBlank()) R.string.trace__ else R.string.trace_rrn,
        ),
        value = displayValue,
        icon = R.drawable.ic_pan,
    )
}
