package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.receipt.IconContainer
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.R

private val itemShape = RoundedCornerShape(12.dp)

private fun Modifier.settingsItemCard(onClick: (() -> Unit)? = null): Modifier {
    val base = this
        .fillMaxWidth()
        .clip(itemShape)
        .background(Color(0xFF0C2C36).copy(alpha = 0.5f))
        .border(1.dp,  Brush.linearGradient(
            colors = listOf(
                Color(0xFF173A46),
                Color(0xFF14BDF6).copy(alpha = 0.5f),
                Color(0xFF35B7E4).copy(alpha = 0.5f)
                ),
        ), itemShape)
    return if (onClick != null) base.clickable(onClick = onClick) else base
}

@Composable
fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        color = SettingsColors.TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun SettingsToggleRow(
    label: String,
    icon: Int,
    iconContentDescription: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .settingsItemCard()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconContainer(
                icon = icon)
            Text(
                text = label,
                color = SettingsColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsColors.Accent,
                uncheckedThumbColor = Color(0xFFB0BEC5),
                uncheckedTrackColor = Color(0xFF144B5B),
                uncheckedBorderColor = Color(0xFF144B5B),
            ),
        )
    }
}

@Composable
fun SettingsNavigationRow(
    label: String,
    icon: Int,
    iconContentDescription: String?,
    value: String? = null,
    valueColor: Color = SettingsColors.Accent,
    labelColor: Color = Color(0XFF5FFBF3),
    valueContent: (@Composable () -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .settingsItemCard(onClick = if (enabled && !isLoading) onClick else null)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconContainer(
                icon = icon)
            Text(
                text = label,
                color = labelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (valueContent != null) {
                valueContent()
            } else if (!value.isNullOrBlank()) {
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = SettingsColors.Accent,
                )
            } else {
                Icon(
                    painter = painterResource(com.danesh.settings.R.drawable.ic_white_arrow_to_let),
                    contentDescription = stringResource(R.string.label_back),
                )
            }
        }
    }
}
