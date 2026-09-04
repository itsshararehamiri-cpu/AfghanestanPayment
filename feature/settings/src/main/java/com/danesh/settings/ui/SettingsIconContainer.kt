package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.danesh.settings.ui.theme.SettingsColors

@Composable
fun SettingsIconContainer(
    icon: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0XFF1F4855).copy(0.6f),
                        Color(0XFF417889),
                        Color(0XFF20C5CA),
                    ),
                ),
                shape = CircleShape,
            ), contentAlignment = Alignment.Center
    ){
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
       //     tint = SettingsColors.Accent,
            modifier = Modifier.size(20.dp),
        )
    }
}
