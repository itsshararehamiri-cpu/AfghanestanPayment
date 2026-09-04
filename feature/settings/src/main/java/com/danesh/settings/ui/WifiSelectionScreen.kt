package com.danesh.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.R
import com.danesh.settings.network.WifiScanResult
import com.danesh.settings.presentation.WifiSelectionViewModel
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun WifiSelectionRoute(
    onBackClick: () -> Unit,
    viewModel: WifiSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingPermissionRequest by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.all { it }) {
            viewModel.refresh()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.hasScanPermission()) {
            viewModel.refresh()
        } else {
            pendingPermissionRequest = true
        }
    }

    LaunchedEffect(pendingPermissionRequest) {
        if (pendingPermissionRequest) {
            pendingPermissionRequest = false
            permissionLauncher.launch(viewModel.requiredPermissions())
        }
    }

    WifiSelectionScreen(
        isLoading = uiState.isLoading,
        networks = uiState.networks,
        selectedSsid = uiState.selectedSsid,
        scanError = uiState.scanError,
        onBackClick = onBackClick,
        onRefresh = {
            if (viewModel.hasScanPermission()) {
                viewModel.refresh()
            } else {
                pendingPermissionRequest = true
            }
        },
        onRequestPermission = {
            pendingPermissionRequest = true
        },
        onOpenLocationSettings = {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        },
        onNetworkSelected = viewModel::selectNetwork,
        saved = uiState.saved,
        onDismissSaved = viewModel::clearSavedFlag,
    )
}

@Composable
fun WifiSelectionScreen(
    isLoading: Boolean,
    networks: List<com.danesh.settings.network.WifiNetworkItem>,
    selectedSsid: String,
    scanError: WifiScanResult.Error?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onNetworkSelected: (String) -> Unit,
    saved: Boolean,
    onDismissSaved: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_wifi_selection_title),
            onBackClick = onBackClick,
        )

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            }
            return@Column
        }

        if (networks.isEmpty()) {
            val messageRes = when (scanError) {
                WifiScanResult.Error.PERMISSION_DENIED ->
                    R.string.settings_support_wifi_selection_permission_denied
                WifiScanResult.Error.LOCATION_DISABLED ->
                    R.string.settings_support_wifi_selection_location_disabled
                WifiScanResult.Error.WIFI_UNAVAILABLE ->
                    R.string.settings_support_wifi_selection_wifi_unavailable
                null -> R.string.settings_support_wifi_selection_empty
            }
            val primaryButtonRes = when (scanError) {
                WifiScanResult.Error.PERMISSION_DENIED ->
                    R.string.settings_support_wifi_selection_grant_permission
                WifiScanResult.Error.LOCATION_DISABLED ->
                    R.string.settings_support_wifi_selection_open_location_settings
                else -> R.string.settings_support_wifi_selection_retry
            }
            val onPrimaryClick = when (scanError) {
                WifiScanResult.Error.PERMISSION_DENIED -> onRequestPermission
                WifiScanResult.Error.LOCATION_DISABLED -> onOpenLocationSettings
                else -> onRefresh
            }

            Box(modifier = Modifier.weight(1f)) {
                SettingsEmptyStateContent(
                    message = stringResource(messageRes),
                )
            }
            SettingsEmptyStateActions(
                primaryButtonTextRes = primaryButtonRes,
                onPrimaryClick = onPrimaryClick,
                onCancelClick = onBackClick,
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            items(networks, key = { it.ssid }) { network ->
                SettingsNavigationRow(
                    label = network.ssid,
                    icon = R.drawable.ic_wifi,
                    iconContentDescription = network.ssid,
                    valueContent = {
                        RadioButton(
                            selected = selectedSsid == network.ssid,
                            onClick = { onNetworkSelected(network.ssid) },
                        )
                    },
                    onClick = { onNetworkSelected(network.ssid) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (saved) {
        AlertDialog(
            onDismissRequest = onDismissSaved,
            text = {
                Text(stringResource(R.string.settings_support_wifi_selection_saved))
            },
            confirmButton = {
                TextButton(onClick = onDismissSaved) {
                    Text(stringResource(R.string.settings_support_confirm))
                }
            },
        )
    }
}
