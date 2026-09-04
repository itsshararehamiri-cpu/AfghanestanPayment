package com.danesh.common.receipt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ElectronicDashedDivider(modifier: Modifier = Modifier,color: Color) {
    //val dividerColor = Color(0XFF5FFBF3).copy(alpha = 0.25f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
@Preview
fun ElectronicDashedDividerPreview() {
    ElectronicDashedDivider(Modifier.fillMaxWidth(), color = Color(0XFF165F73))
}
