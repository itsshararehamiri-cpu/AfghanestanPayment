package com.danesh.common.receipt

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/** متن فیلد 55/56 میزبان روی رسید الکترونیکی — اول 55، سپس 56. */
@Composable
fun ElectronicReceiptHostTextRow(
    text: String,
    modifier: Modifier = Modifier,
    secondText: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE8F4F6),
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        secondText?.takeIf { it.isNotBlank() }?.let { second ->
            Text(
                text = second,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE8F4F6),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }
    }
}
