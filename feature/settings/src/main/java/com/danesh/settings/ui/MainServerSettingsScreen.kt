package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.connection.ConnectionChannel
import com.danesh.settings.ConnectionTypeSelectionBottomSheet
import com.danesh.settings.R
import com.danesh.settings.model.MainServerSettingsUiState
import com.danesh.settings.presentation.MainServerSettingsViewModel
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun MainServerSettingsRoute(
    onBackClick: () -> Unit,
    onNavigateToWifiSelection: () -> Unit,
    viewModel: MainServerSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ipEmpty = stringResource(R.string.settings_support_ip_error_empty)
    val ipInvalid = stringResource(R.string.settings_support_ip_error_invalid)
    val portEmpty = stringResource(R.string.settings_support_port_error_empty)
    val portInvalid = stringResource(R.string.settings_support_port_error_invalid)
    val idleInvalid = stringResource(R.string.settings_support_idle_time_error_invalid)

    androidx.compose.runtime.LaunchedEffect(uiState.navigateToWifiSelection) {
        if (uiState.navigateToWifiSelection) {
            viewModel.consumeWifiNavigationRequest()
            onNavigateToWifiSelection()
        }
    }

    MainServerSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onConnectionTypeChange = viewModel::setConnectionType,
        onServerIpChange = viewModel::setServerIp,
        onServerPortChange = viewModel::setServerPort,
        onTmsIpChange = viewModel::setTmsIp,
        onTmsPortChange = viewModel::setTmsPort,
        onIdleTimeChange = viewModel::setIdleTimeMinutes,
        onSaveClick = {
            viewModel.save(
                ipEmptyError = ipEmpty,
                ipInvalidError = ipInvalid,
                portEmptyError = portEmpty,
                portInvalidError = portInvalid,
                idleInvalidError = idleInvalid,
            )
        },
        onDismissSavedMessage = viewModel::clearSavedFlag,
    )
}

@Composable
fun MainServerSettingsScreen(
    uiState: MainServerSettingsUiState,
    onBackClick: () -> Unit,
    onConnectionTypeChange: (ConnectionChannel) -> Unit,
    onServerIpChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onTmsIpChange: (String) -> Unit,
    onTmsPortChange: (String) -> Unit,
    onIdleTimeChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissSavedMessage: () -> Unit,
) {
    var showConnectionTypeSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_main_server_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_connection_type_label),
                icon = R.drawable.ic_connect_to_net,
                iconContentDescription = stringResource(R.string.settings_support_connection_type_label),
                value = connectionTypeLabel(uiState.connectionType),
                onClick = { showConnectionTypeSheet = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle(
                title = stringResource(R.string.settings_support_main_server_host_section),
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTextField(
                value = uiState.serverIp,
                onValueChange = onServerIpChange,
                label = stringResource(R.string.settings_support_server_ip),
                placeholder = stringResource(R.string.settings_support_ip_hint),
                errorMessage = uiState.serverIpError,
                inputFilter = SettingsTextInputFilter.IpAddress,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTextField(
                value = uiState.serverPort,
                onValueChange = onServerPortChange,
                label = stringResource(R.string.settings_support_server_port),
                placeholder = stringResource(R.string.settings_support_port_hint),
                errorMessage = uiState.serverPortError,
                inputFilter = SettingsTextInputFilter.DigitsOnly,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle(
                title = stringResource(R.string.settings_support_tms_server_section),
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTextField(
                value = uiState.tmsIp,
                onValueChange = onTmsIpChange,
                label = stringResource(R.string.settings_support_tms_server_ip),
                placeholder = stringResource(R.string.settings_support_ip_hint),
                errorMessage = uiState.tmsIpError,
                inputFilter = SettingsTextInputFilter.IpAddress,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTextField(
                value = uiState.tmsPort,
                onValueChange = onTmsPortChange,
                label = stringResource(R.string.settings_support_tms_server_port),
                placeholder = stringResource(R.string.settings_support_port_hint),
                errorMessage = uiState.tmsPortError,
                inputFilter = SettingsTextInputFilter.DigitsOnly,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionTitle(
                title = stringResource(R.string.settings_support_main_server_other_section),
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTextField(
                value = uiState.idleTimeMinutes,
                onValueChange = onIdleTimeChange,
                label = stringResource(R.string.settings_support_idle_time_minutes),
                placeholder = stringResource(R.string.settings_support_idle_time_hint),
                errorMessage = uiState.idleTimeError,
                inputFilter = SettingsTextInputFilter.PositiveInteger,
            )

            uiState.validationSummary?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GradientActionButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = stringResource(R.string.settings_support_confirm),
                onClick = onSaveClick,
            )
        }
    }

    if (showConnectionTypeSheet) {
        ConnectionTypeSelectionBottomSheet(
            selectedType = uiState.connectionType,
            onTypeSelected = {
                onConnectionTypeChange(it)
                showConnectionTypeSheet = false
            },
            onDismissRequest = { showConnectionTypeSheet = false },
        )
    }

    if (uiState.savedSuccessfully) {
        AlertDialog(
            onDismissRequest = onDismissSavedMessage,
            text = {
                Text(
                    text = stringResource(R.string.settings_support_main_server_saved),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissSavedMessage) {
                    Text(stringResource(R.string.settings_support_confirm))
                }
            },
        )
    }
}

@Composable
private fun connectionTypeLabel(type: ConnectionChannel): String = when (type) {
    ConnectionChannel.WIFI -> stringResource(R.string.settings_support_connection_type_wifi)
    ConnectionChannel.GPRS -> stringResource(R.string.settings_support_connection_type_gprs)
}
