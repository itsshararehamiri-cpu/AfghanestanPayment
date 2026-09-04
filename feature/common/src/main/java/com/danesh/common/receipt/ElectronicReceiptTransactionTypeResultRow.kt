package com.danesh.common.receipt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.R

@Composable
fun ElectronicReceiptTransactionTypeResultRow(type: String, icon: Int, isSuccess: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
              //  tint = Color(0XFF5FFBF3),
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = type,
                color = Color(0xFFFFFFFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(if (isSuccess) R.drawable.ic_success else R.drawable.ic_unsucess),
                contentDescription = null,
                tint = Color(if(isSuccess) 0XFF00FFD4 else 0XFFFF4F52),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(if (isSuccess) R.string.success_result else R.string.unsuccess_result),
                color = Color(if(isSuccess) 0XFF00FFD4 else 0XFFFF4F52),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}