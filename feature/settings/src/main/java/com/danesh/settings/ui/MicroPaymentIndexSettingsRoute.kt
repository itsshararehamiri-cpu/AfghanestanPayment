package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.R
import com.danesh.settings.presentation.MicroPaymentIndexSettingsViewModel

@Composable
fun MicroPaymentIndexSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: MicroPaymentIndexSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emptyError = stringResource(R.string.settings_support_micro_payment_index_error_empty)
    val invalidError = stringResource(R.string.settings_support_micro_payment_index_error_invalid)
    val rangeError = stringResource(R.string.settings_support_micro_payment_index_error_range)
    val savedMessage = stringResource(R.string.settings_support_micro_payment_index_saved)

    MicroPaymentIndexSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onInputChange = viewModel::onInputChange,
        onSaveClick = {
            viewModel.save(
                emptyError = emptyError,
                invalidError = invalidError,
                rangeError = rangeError,
                savedMessage = savedMessage,
            )
        },
        onDismissSavedMessage = {
            viewModel.clearSavedMessage()
            onBackClick()
        },
    )
}
