package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.network.NetworkConnectionType
import com.danesh.settings.R
import com.danesh.settings.ui.theme.SettingsColors

/**
 * نتیجه بررسی وضعیت ارتباطی:
 * - نوع اتصال: WiFi یا GPRS
 * - وضعیت اتصال به سرور (Telnet به سوییچ): موفق یا ناموفق
 */
@Composable
fun ConnectionStatusCheckDialog(
    connectionType: NetworkConnectionType,
    isChecking: Boolean,
    isServerConnected: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsColors.Background,
        title = {
            Text(
                text = stringResource(R.string.settings_merchant_connection_status),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SettingsColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ConnectionStatusResultCard(
                    label = stringResource(R.string.settings_support_connection_type_label),
                    value = stringResource(connectionTypeLabelRes(connectionType)),
                    valueColor = when (connectionType) {
                        NetworkConnectionType.NONE -> Color(0xFFFF5252)
                        else -> Color(0xFF5FFBF3)
                    },
                    showDot = connectionType != NetworkConnectionType.NONE,
                    dotColor = Color(0xFF5FFBF3),
                )

                if (isChecking) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C2C36).copy(alpha = 0.55f))
                            .border(1.dp, Color(0xFF173A46), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SettingsColors.Accent,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.settings_support_connection_checking),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SettingsColors.TextPrimary,
                        )
                    }
                } else {
                    val success = isServerConnected
                    ConnectionStatusResultCard(
                        label = stringResource(R.string.settings_support_server_connection_label),
                        value = stringResource(
                            if (success) {
                                R.string.settings_support_server_connection_success
                            } else {
                                R.string.settings_support_server_connection_failed
                            },
                        ),
                        valueColor = if (success) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        showDot = true,
                        dotColor = if (success) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.settings_support_confirm),
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingsColors.Accent,
                )
            }
        },
    )
}

@Composable
private fun ConnectionStatusResultCard(
    label: String,
    value: String,
    valueColor: Color,
    showDot: Boolean,
    dotColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C2C36).copy(alpha = 0.55f))
            .border(1.dp, Color(0xFF173A46), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SettingsColors.TextPrimary.copy(alpha = 0.85f),
            fontSize = 13.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
            )
        }
    }
}

private fun connectionTypeLabelRes(type: NetworkConnectionType): Int = when (type) {
    NetworkConnectionType.WIFI -> R.string.settings_support_connection_type_wifi
    NetworkConnectionType.GPRS -> R.string.settings_support_connection_type_gprs
    NetworkConnectionType.NONE -> R.string.settings_support_connection_type_none
}
