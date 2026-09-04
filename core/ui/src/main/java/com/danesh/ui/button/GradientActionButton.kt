package com.danesh.ui.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GradientActionButton(
    modifier: Modifier,

    text: String,
    icon: Int? = null,
    onClick: () -> Unit,
) {
    val shape =
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00FFD4),
                            Color(0xFF0E6268),
                        ),
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (icon != null)
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "",
                        modifier = Modifier.size(22.dp)
                    )
                Text(
                    text = text,
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall,

                    )
            }
        }
}

@Composable
@Preview
fun GradientActionButtonPreview() {
    GradientActionButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        text = "استعلام قبض",
        onClick = {},
    )
}