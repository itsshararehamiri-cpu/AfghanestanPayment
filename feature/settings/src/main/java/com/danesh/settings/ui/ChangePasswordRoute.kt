package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.R
import com.danesh.settings.presentation.ChangePasswordViewModel

@Composable
fun ChangePasswordRoute(
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit = onBackClick,
    onPasswordChanged: () -> Unit = onBackClick,
    mandatory: Boolean = false,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val insecurePasswordError = stringResource(R.string.settings_change_password_error_insecure)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPasswordChanged()
        }
    }

    ChangePasswordScreen(
        onBackClick = if (mandatory) null else onBackClick,
        onCancelClick = onCancelClick,
        mandatory = mandatory,
        externalNewPasswordError = uiState.newPasswordError,
        onNewPasswordChange = viewModel::clearError,
        onConfirmClick = { newPassword ->
            viewModel.changePassword(
                newPassword = newPassword,
                insecurePasswordError = insecurePasswordError,
            )
        },
    )
}
