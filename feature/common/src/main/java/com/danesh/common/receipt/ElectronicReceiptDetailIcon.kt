package com.danesh.common.receipt
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed interface ElectronicReceiptDetailIcon {
    data class Vector(
        val imageVector: ImageVector,
        val tint: Color = Color(0XFF5FFBF3).copy(alpha = 0.85f),
        val size: Dp = 20.dp,
    ) : ElectronicReceiptDetailIcon

    data class Drawable(
        @DrawableRes val resId: Int,
        val tint: Color = Color(0XFF5FFBF3).copy(alpha = 0.85f),
        val size: Dp = 20.dp,
    ) : ElectronicReceiptDetailIcon
}