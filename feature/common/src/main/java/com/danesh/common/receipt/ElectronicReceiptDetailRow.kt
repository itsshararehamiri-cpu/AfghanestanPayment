package com.danesh.common.receipt
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ElectronicReceiptDetailRow(
    label: String,
     @DrawableRes  icon: Int,
    modifier: Modifier = Modifier,
    labelColor: Color = Color(0xFFB0BEC5),
    value: String? = null,
    valueColor: Color = Color(0xFFFFFFFF),
    valueFontWeight: FontWeight = FontWeight.Normal,
    valueContent: (@Composable () -> Unit)? = null,
    verticalPadding: Dp = 0.dp,
) {
    Row(
        modifier = modifier
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
            IconContainer(icon=icon)
            Text(
                text = label,
                color = labelColor,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier.wrapContentWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            when {
                valueContent != null -> valueContent()
                value != null -> {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = value,
                        color = valueColor,
                        fontSize = 13.sp,
                        fontWeight = valueFontWeight,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Visible,
                        style = MaterialTheme.typography.bodySmall,
                    )}
                }
            }
        }
    }
}
@Composable
@Preview
fun ElectronicReceiptDetailRowPreview(){
    ElectronicReceiptDetailRow(
        label = "کد کارتخوان",
        value = "سیییییییییییییی",
        icon = com.danesh.common.R.drawable.ic_white_arrow_to_right,
    )
}