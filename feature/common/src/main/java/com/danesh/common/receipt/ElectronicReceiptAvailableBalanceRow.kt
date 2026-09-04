package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.danesh.common.R

@Composable
fun ElectronicReceiptAvailableBalanceRow(availableBalance: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.label_available_balance),
        value = availableBalance,
        icon = com.danesh.ui.R.drawable.ic_money_send,
        valueColor = Color(0xFF01F0B4),
        valueFontWeight = FontWeight.Bold,
    )
}