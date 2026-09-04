package com.danesh.common.receipt

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.danesh.common.R
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.TransactionDateTimeFormatter

@Composable
fun ElectronicReceiptDateTimeRow(date: String, time: String) {
    val context = ReceiptDateTimeContexts.current()
    ElectronicReceiptDetailRow(
        label = stringResource(R.string.label_datetime),
        value = TransactionDateTimeFormatter.formatDisplay(
             date,
           time,
            locale = context.locale,
            calendarStyle = context.calendarStyle,
        ),
        icon = com.danesh.ui.R.drawable.ic_calendar_2,
    )
}
