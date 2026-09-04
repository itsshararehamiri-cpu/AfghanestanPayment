package com.danesh.common.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.R

@Composable
fun ElectronicReceiptCardInfoRow(pan: String,issuer: String, verticalPadding: Dp = 0.dp,   valueContent: (@Composable () -> Unit)? = null,) {//
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
//    ElectronicReceiptDetailRow(
//        label = stringResource(R.string.label_card_info),
//        value = pan,
//        icon = R.drawable.ic_pan,
//    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(icon= R.drawable.ic_pan)
            Text(
                text = stringResource(R.string.label_card_info),
                color = Color(0xFFB0BEC5),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier.wrapContentWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
         //   when {
//                valueContent != null -> valueContent()
           //     value != null -> {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = pan.maskPanForReceipt(),
                    color = Color(0xFFFFFFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
             //   }
            //}
       // }
    }
}}