package com.danesh.common.receipt.paper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danesh.common.getFontSize
import com.danesh.common.getFontWeight
import com.danesh.ui.theme.withAppFont

@Composable
fun ReceiptItem(
    modifier: Modifier = Modifier,
    first: String,
    second: String,
    textColor: Color,
    isPaperReceipt: Boolean = false
) {
    val context = LocalContext.current
    Column (
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = first,
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 0.dp )
                .layoutId("first"),
            color = textColor,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = getFontSize(isPaperReceipt, context),
                    fontWeight = getFontWeight(isPaperReceipt, context)
                ).withAppFont(),
            textAlign = TextAlign.End
        )
        //Spacer(modifier = Modifier.weight(1f))
        Text(
            text = second,
            modifier = Modifier
                .wrapContentWidth()
                .padding(start =  0.dp )
                .layoutId("second"),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = getFontSize(isPaperReceipt, context),
                fontWeight = getFontWeight(isPaperReceipt, context)
            ).withAppFont(),
            textAlign = TextAlign.Start
        )
    }
}
