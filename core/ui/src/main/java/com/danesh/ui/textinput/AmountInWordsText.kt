package com.danesh.ui.textinput

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.theme.appTextStyle

/** نمایش مبلغ به حروف زیر فیلد ورود مبلغ. */
@Composable
fun AmountInWordsText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        color = Color(0XFF5FFBF3),
        textAlign = TextAlign.Start,
        style = appTextStyle(
            base = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
        ),
    )
}
