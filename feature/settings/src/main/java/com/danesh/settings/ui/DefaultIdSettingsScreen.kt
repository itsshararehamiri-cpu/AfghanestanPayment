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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.R
import com.danesh.settings.model.DefaultIdSettingsUiState
import com.danesh.settings.presentation.DefaultIdSettingsViewModel
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun DefaultIdSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: DefaultIdSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emptyError = stringResource(R.string.settings_support_default_id_error_empty)
    val invalidError = stringResource(R.string.settings_support_default_id_error_invalid)
    val lengthError = stringResource(R.string.settings_support_default_id_error_length)

    DefaultIdSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEnabledChange = viewModel::setEnabled,
        onValueChange = viewModel::setValue,
        onSaveClick = {
            viewModel.save(
                emptyError = emptyError,
                invalidError = invalidError,
                lengthError = lengthError,
            )
        },
        onDismissSavedMessage = viewModel::clearSavedFlag,
    )
}

@Composable
fun DefaultIdSettingsScreen(
    uiState: DefaultIdSettingsUiState,
    onBackClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissSavedMessage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_default_id_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsToggleRow(
                label = stringResource(R.string.settings_support_default_id_enabled),
                icon = R.drawable.ic_ip,
                iconContentDescription = stringResource(R.string.settings_support_default_id_enabled),
                checked = uiState.enabled,
                onCheckedChange = onEnabledChange,
            )

            if (uiState.enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSectionTitle(
                    title = stringResource(R.string.settings_support_default_id_value_section),
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsTextField(
                    value = uiState.value,
                    onValueChange = onValueChange,
                    label = stringResource(R.string.settings_support_default_id_value_label),
                    placeholder = stringResource(R.string.settings_support_default_id_value_hint),
                    errorMessage = uiState.valueError,
                    inputFilter = SettingsTextInputFilter.DigitsOnly,
                )
            }

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

    if (uiState.savedSuccessfully) {
        AlertDialog(
            onDismissRequest = onDismissSavedMessage,
            text = {
                Text(stringResource(R.string.settings_support_default_id_saved))
            },
            confirmButton = {
                TextButton(onClick = onDismissSavedMessage) {
                    Text(stringResource(R.string.settings_support_confirm))
                }
            },
        )
    }
}
