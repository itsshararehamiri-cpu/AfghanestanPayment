package com.danesh.common


import androidx.annotation.ColorRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier, isPaperReceipt: Boolean) {
    HorizontalDivider(
        modifier = modifier.padding(top = if (isPaperReceipt) 5.dp else 10.dp),
        color = if (isPaperReceipt) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
        thickness = if (isPaperReceipt) 1.dp else 1.dp
    )
}

@Composable
fun DashedDivider(@ColorRes colorId: Color) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        drawLine(
            color = colorId,
            strokeWidth = 5f,
            start = Offset(10f, 0f),
            end = Offset(size.width - 10, 0f),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun DottedDivider(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .background(Color.Gray, shape = DottedShape(step = 10.dp))
            .fillMaxWidth()
            .height(1.dp)
    )
}
//@Composable
//fun DividerLightGray(
//    modifier: Modifier
//) {
//    HorizontalDivider(modifier = modifier, color = Color.LightGray)
//}
data class DottedShape(
    val step: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        val stepPx = with(density) { step.toPx() }
        val stepsCount = (size.width / stepPx).roundToInt()
        val actualStep = size.width / stepsCount
        val dotSize = Size(width = actualStep / 2, height = size.height)
        for (i in 0 until stepsCount) {
            addRect(
                Rect(
                    offset = Offset(x = i * actualStep, y = 0f),
                    size = dotSize
                )
            )
        }
        close()
    })
}

@Composable
fun Line(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    thickness: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color = color)
    )
}

@Composable
fun DottedLine(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    thickness: Dp = 2.dp,
    spacing: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .drawBehind {
                val y = size.height / 2
                val dashWidthPx = spacing.toPx() * 2
                val dashLengthPx = spacing.toPx()
                repeat((size.width / dashWidthPx).toInt()) {
                    drawLine(
                        color = color,
                        start = Offset(it * dashWidthPx, y),
                        end = Offset(it * dashWidthPx + dashLengthPx, y),
                        strokeWidth = thickness.toPx()
                    )
                }
            }
    )
}
