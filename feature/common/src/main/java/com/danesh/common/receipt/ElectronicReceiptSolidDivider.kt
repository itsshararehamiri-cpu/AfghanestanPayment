package com.danesh.common.receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PurchaseSolidDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = Color(0XFF5FFBF3).copy(alpha = 0.15f),
        thickness = 1.dp,
    )
}
