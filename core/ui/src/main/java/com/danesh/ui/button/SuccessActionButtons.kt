package com.danesh.ui.button
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
 fun SuccessActionButtons(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onPrintReceiptClick: () -> Unit,
    homeEnabled: Boolean = true,
    printReceiptLabel: String = stringResource(com.danesh.ui.R.string.balance_print_receipt),
) {
    val shape =RoundedCornerShape(12.dp)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {


        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(shape)
                .border(
                    width = 1.5.dp,
                    color = Color(0XFF5FFBF3),
                    shape = shape,
                )
                .clickable(onClick = onPrintReceiptClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Print,
                    contentDescription = null,
                    tint = Color(0XFF5FFBF3),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = printReceiptLabel,
                    color = Color(0XFF5FFBF3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }


        GradientActionButton(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .alpha(if (homeEnabled) 1f else 0.45f)
                .clickable(enabled = homeEnabled, onClick = onHomeClick),
            text = stringResource(com.danesh.ui.R.string.balance_home),
            onClick = { if (homeEnabled) onHomeClick() },
        )
    }
}