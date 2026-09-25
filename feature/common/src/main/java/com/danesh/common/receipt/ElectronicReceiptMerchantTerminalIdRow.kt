package com.danesh.common.receipt

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.danesh.common.R

@Composable
fun ElectronicReceiptMerchantTerminalIdRow(terminalId: String,merchantId: String) {
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.terminal_merchant_label),
        value = "${merchantId}/${terminalId}",
        icon = R.drawable.ic_terminal_merchant,
    )
}

/** کد کارتخوان (سداد)؛ اگر خالی باشد چیزی نمایش داده نمی‌شود. */
@Composable
fun ElectronicReceiptTerminalUniqueCodeRow(terminalUniqueCode: String) {
    LaunchedEffect(terminalUniqueCode) {
        Log.d("TUC", "electronic receipt terminalUniqueCode='$terminalUniqueCode' shown=${terminalUniqueCode.isNotBlank()}")
    }
    if (terminalUniqueCode.isBlank()) return
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.label_terminal_unique_code),
        value = terminalUniqueCode,
        icon = R.drawable.ic_terminal_merchant,
    )
}
