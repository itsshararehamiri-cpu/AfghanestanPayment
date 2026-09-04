package com.danesh.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.ui.R
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appTextStyle

@Composable
fun BottomSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BottomSheetColors.DragHandle.copy(alpha = 0.6f)),
        )
    }
}

@Composable
fun BottomSheetHeader(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AppColors.TextOnBackground,
            style = appTextStyle(
                base = MaterialTheme.typography.bodySmall,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        IconButton(onClick = onCloseClick) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, BottomSheetColors.CloseBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.bottom_sheet_close),
                    tint = AppColors.TextOnBackground,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

internal object BottomSheetColors {
    val DragHandle = androidx.compose.ui.graphics.Color(0xFF848484)
    val CloseBorder = androidx.compose.ui.graphics.Color(0xFF144B5B)
}
