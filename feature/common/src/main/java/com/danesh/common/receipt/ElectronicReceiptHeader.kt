package com.danesh.common.receipt

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
 fun ElectronicReceiptHeader(message: String,isSuccess: Boolean) {
    Log.d("BalanceFlow", "ElectronicReceiptHeader | message=$message | isSuccess=$isSuccess")
    ElectronicReceiptSuccessBadge(isSuccess = isSuccess)

     Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = message,
        color = Color(if(isSuccess)0XFF01F0B4 else 0XFFFF4F52),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall
    )
}