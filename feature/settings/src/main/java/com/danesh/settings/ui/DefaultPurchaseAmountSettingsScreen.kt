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
import com.danesh.settings.model.DefaultPurchaseAmountSettingsUiState
import com.danesh.settings.presentation.DefaultPurchaseAmountSettingsViewModel
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun DefaultPurchaseAmountSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: DefaultPurchaseAmountSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emptyError = stringResource(R.string.settings_merchant_default_amount_error_empty)
    val invalidError = stringResource(R.string.settings_merchant_default_amount_error_invalid)
    val belowMinimumError = stringResource(R.string.settings_merchant_default_amount_error_below_min)
    val tooManyDigitsError = stringResource(R.string.settings_merchant_default_amount_error_too_long)

    DefaultPurchaseAmountSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEnabledChange = viewModel::setEnabled,
        onAmountChange = viewModel::onAmountChange,
        onSaveClick = {
            viewModel.save(
                emptyError = emptyError,
                invalidError = invalidError,
                belowMinimumError = belowMinimumError,
                tooManyDigitsError = tooManyDigitsError,
            )
        },
        onDismissSavedMessage = {
            viewModel.clearSavedFlag()
            onBackClick()
        },
    )
}

@Composable
fun DefaultPurchaseAmountSettingsScreen(
    uiState: DefaultPurchaseAmountSettingsUiState,
    onBackClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onAmountChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissSavedMessage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_merchant_default_amount_title),
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
                label = stringResource(R.string.settings_merchant_default_amount_enabled),
                icon = R.drawable.ic_card_tick,
                iconContentDescription = stringResource(R.string.settings_merchant_default_amount_enabled),
                checked = uiState.enabled,
                onCheckedChange = onEnabledChange,
            )

            if (uiState.enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSectionTitle(
                    title = stringResource(R.string.settings_merchant_default_amount_value_section),
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsTextField(
                    value = uiState.amountDigits,
                    onValueChange = onAmountChange,
                    label = stringResource(R.string.settings_merchant_default_amount_value_label),
                    placeholder = stringResource(R.string.settings_merchant_default_amount_value_hint),
                    errorMessage = uiState.amountError,
                    inputFilter = SettingsTextInputFilter.DigitsOnly,
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
                Text(stringResource(R.string.settings_merchant_default_amount_saved))
            },
            confirmButton = {
                TextButton(onClick = onDismissSavedMessage) {
                    Text(stringResource(R.string.settings_support_confirm))
                }
            },
        )
    }
}
