package com.danesh.common.receipt.paper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.danesh.common.R
import com.danesh.common.locale.ReceiptDateTimeContexts
import com.danesh.common.locale.TransactionDateTimeFormatter

@Composable
fun DateTime(
    modifier: Modifier = Modifier,
    date: String,
    time: String = "",
    textColor: Color,
    isPaperReceipt: Boolean = false,
) {
    val context = ReceiptDateTimeContexts.current()
    val formatted = when {
        date.isNotBlank() && time.isNotBlank() ->
            TransactionDateTimeFormatter.formatDisplay(
              date,
               time,
                locale = context.locale,
                calendarStyle = context.calendarStyle,
            )
        date.isNotBlank() -> TransactionDateTimeFormatter.formatDate(
            dateYyyyMmDd = date,
            locale = context.locale,
            calendarStyle = context.calendarStyle,
        )
        time.isNotBlank() -> TransactionDateTimeFormatter.formatTime(time)
        else -> ""
    }
    ReceiptItem(
        modifier,
        first = stringResource(R.string.date_time),
        second = formatted,
        textColor = textColor,
        isPaperReceipt = isPaperReceipt,
    )
}
