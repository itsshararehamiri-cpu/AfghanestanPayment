package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.connection.ConnectionChannel
import com.danesh.settings.R
import com.danesh.settings.model.BackupPlatformUiState
import com.danesh.settings.presentation.BackupPlatformViewModel
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun BackupPlatformRoute(
    onBackClick: () -> Unit,
    viewModel: BackupPlatformViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackupPlatformScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onBackupEnabledChange = viewModel::setBackupEnabled,
    )
}

@Composable
fun BackupPlatformScreen(
    uiState: BackupPlatformUiState,
    onBackClick: () -> Unit,
    onBackupEnabledChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_backup_platform),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            if (!uiState.dualChannelConfigured) {
                Text(
                    text = stringResource(R.string.settings_support_backup_platform_not_configured),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
                return@Column
            }

            uiState.alternateBackupChannel?.let { channel ->
                SettingsToggleRow(
                    label = backupChannelLabel(channel),
                    icon = backupChannelIcon(channel),
                    iconContentDescription = backupChannelLabel(channel),
                    checked = uiState.backupEnabled,
                    onCheckedChange = onBackupEnabledChange,
                )
            } ?: run {
                Text(
                    text = stringResource(R.string.settings_support_backup_platform_no_active_connection),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun backupChannelLabel(channel: ConnectionChannel): String = when (channel) {
    ConnectionChannel.GPRS -> stringResource(R.string.settings_support_backup_platform_gprs_inactive)
    ConnectionChannel.WIFI -> stringResource(R.string.settings_support_backup_platform_wifi_inactive)
}

private fun backupChannelIcon(channel: ConnectionChannel): Int = when (channel) {
    ConnectionChannel.GPRS -> R.drawable.ic_connect_to_net
    ConnectionChannel.WIFI -> R.drawable.ic_wifi
}
