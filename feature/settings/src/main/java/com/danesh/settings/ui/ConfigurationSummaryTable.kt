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
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.ui.theme.SettingsColors

private val summaryCardShape = RoundedCornerShape(14.dp)
private val summaryRowShape = RoundedCornerShape(10.dp)

@Composable
fun ConfigurationSummaryTable(
    summary: InitialConfigurationSummary,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        title?.let {
            Text(
                text = it,
                color = SettingsColors.Accent,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF0C2C36).copy(alpha = 0.72f),
                    shape = summaryCardShape,
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
                    shape = summaryCardShape,
                )
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ConfigurationSummaryTableRow(
                label = stringResource(R.string.settings_initial_configuration_hardware_serial),
                value = summary.hardwareSerial,
            )
            ConfigurationSummaryTableRow(
                label = stringResource(R.string.settings_initial_configuration_terminal_id),
                value = summary.terminalId,
            )
            ConfigurationSummaryTableRow(
                label = stringResource(R.string.settings_initial_configuration_merchant_id),
                value = summary.merchantId,
            )
            ConfigurationSummaryTableRow(
                label = stringResource(R.string.settings_initial_configuration_app_version),
                value = summary.appVersion,
            )
            ConfigurationSummaryTableRow(
                label = stringResource(R.string.settings_initial_configuration_program_date),
                value = summary.programDate,
            )
        }
    }
}

@Composable
private fun ConfigurationSummaryTableRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF082028).copy(alpha = 0.65f),
                shape = summaryRowShape,
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1A4554).copy(alpha = 0.8f),
                shape = summaryRowShape,
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
