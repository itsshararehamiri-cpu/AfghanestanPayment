package com.danesh.common.receipt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.R

@Composable
fun ElectronicReceiptSuccessResponseMessageRow(    modifier: Modifier = Modifier,
                                                   verticalPadding: Dp = 0.dp,) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = stringResource(com.danesh.common.R.string.success_result),
            color =Color(0xFFB0BEC5),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )



    }
}