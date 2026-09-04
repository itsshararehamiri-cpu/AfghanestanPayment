package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danesh.common.GetPasswordScreen
import com.danesh.settings.R
import com.danesh.settings.domain.MerchantPasswordValidator

private enum class ChangePasswordStep {
    NEW,
    CONFIRM,
}

@Composable
fun ChangePasswordScreen(
    onBackClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    mandatory: Boolean = false,
    onConfirmClick: (newPassword: String) -> Unit = {},
    externalNewPasswordError: String? = null,
    onNewPasswordChange: () -> Unit = {},
) {
    var step by rememberSaveable { mutableStateOf(ChangePasswordStep.NEW) }
    var storedNewPassword by rememberSaveable { mutableStateOf("") }
    var pinValue by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val insecurePasswordError = stringResource(R.string.settings_change_password_error_insecure)
    val mismatchError = stringResource(R.string.settings_change_password_error_mismatch)
    val screenTitle = stringResource(
        if (mandatory) {
            R.string.settings_change_password_mandatory_title
        } else {
            R.string.settings_change_management_password
        },
    )
    val instruction = when (step) {
        ChangePasswordStep.NEW -> stringResource(R.string.settings_change_password_new)
        ChangePasswordStep.CONFIRM -> stringResource(R.string.settings_change_password_confirm)
    }
    val errorMessage = localError ?: externalNewPasswordError
    val resolvedCancelClick: () -> Unit = when (step) {
        ChangePasswordStep.CONFIRM -> {
            {
                step = ChangePasswordStep.NEW
                pinValue = ""
                localError = null
                onNewPasswordChange()
            }
        }
        ChangePasswordStep.NEW -> {
            { onCancelClick?.invoke() ?: onBackClick?.invoke() }
        }
    }

    LaunchedEffect(externalNewPasswordError) {
        if (externalNewPasswordError != null) {
            step = ChangePasswordStep.NEW
            storedNewPassword = ""
            pinValue = ""
        }
    }

    GetPasswordScreen(
        pinValue = pinValue,
        title = screenTitle,
        instruction = instruction,
        hintText = errorMessage,
        hintColor = Color(0xFFFF8A80),
        showRetryHint = errorMessage != null,
        onBackClick = when {
            mandatory -> null
            step == ChangePasswordStep.CONFIRM -> {
                {
                    step = ChangePasswordStep.NEW
                    pinValue = ""
                    localError = null
                    onNewPasswordChange()
                }
            }
            else -> onBackClick
        },
        onCancelClick = resolvedCancelClick,
        onPinValueChange = { value ->
            pinValue = value
            if (localError != null) {
                localError = null
                onNewPasswordChange()
            }
        },
        onPinComplete = { completedPin ->
            when (step) {
                ChangePasswordStep.NEW -> {
                    if (!MerchantPasswordValidator.isSecure(completedPin)) {
                        pinValue = ""
                        localError = insecurePasswordError
                        return@GetPasswordScreen
                    }
                    storedNewPassword = completedPin
                    pinValue = ""
                    localError = null
                    onNewPasswordChange()
                    step = ChangePasswordStep.CONFIRM
                }
                ChangePasswordStep.CONFIRM -> {
                    if (completedPin != storedNewPassword) {
                        pinValue = ""
                        localError = mismatchError
                        return@GetPasswordScreen
                    }
                    onConfirmClick(storedNewPassword)
                }
            }
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun ChangePasswordScreenPreview() {
    ChangePasswordScreen()
}
