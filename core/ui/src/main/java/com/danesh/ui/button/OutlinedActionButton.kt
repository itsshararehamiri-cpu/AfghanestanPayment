package com.danesh.ui.button
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
 fun OutlinedActionButton(modifier: Modifier,
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00FFD4),
            Color(0xFF20C5CA),
        ),
    )
    Box(
        modifier = modifier

            .border(width = 1.5.dp, brush = borderGradient, shape = shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color =Color(0XFF5FFBF3),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall
        )
    }
}
@Composable
@Preview
fun OutlinedActionButtonPreview(){
    OutlinedActionButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        text = "استعلام قبض",
        onClick = {},
    )
}