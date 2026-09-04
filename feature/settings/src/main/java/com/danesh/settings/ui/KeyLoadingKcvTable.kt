package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.ui.theme.SettingsColors

private val kcvCardShape = RoundedCornerShape(14.dp)
private val kcvRowShape = RoundedCornerShape(10.dp)

@Composable
fun KeyLoadingKcvTable(
    kcvSummary: KeyLoadingKcvSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_key_loading_kcv_title),
            color = SettingsColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF0C2C36).copy(alpha = 0.72f),
                    shape = kcvCardShape,
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF173A46),
                            Color(0xFF14BDF6).copy(alpha = 0.55f),
                            Color(0xFF35B7E4).copy(alpha = 0.45f),
                        ),
                    ),
                    shape = kcvCardShape,
                )
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KeyLoadingKcvRow(
                label = stringResource(R.string.settings_kcv_master_key),
                value = kcvSummary.master,
            )
            if (!kcvSummary.masterOnly) {
                if (kcvSummary.mac != "-") {
                    KeyLoadingKcvRow(
                        label = stringResource(R.string.settings_kcv_mac_key),
                        value = kcvSummary.mac,
                    )
                }
                if (kcvSummary.pin != "-") {
                    KeyLoadingKcvRow(
                        label = stringResource(R.string.settings_kcv_pin_key),
                        value = kcvSummary.pin,
                    )
                }
                if (kcvSummary.data != "-") {
                    KeyLoadingKcvRow(
                        label = stringResource(R.string.settings_kcv_data_key),
                        value = kcvSummary.data,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyLoadingKcvRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF082028).copy(alpha = 0.65f),
                shape = kcvRowShape,
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1A4554).copy(alpha = 0.8f),
                shape = kcvRowShape,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = SettingsColors.TextPrimary.copy(alpha = 0.92f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            color = SettingsColors.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
