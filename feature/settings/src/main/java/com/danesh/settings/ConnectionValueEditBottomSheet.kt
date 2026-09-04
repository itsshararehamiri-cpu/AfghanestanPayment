package com.danesh.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.danesh.common.connection.ConnectionAddressValidator
import com.danesh.settings.ui.ConnectionValueEditSheetContent
import com.danesh.settings.ui.theme.SettingsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionValueEditBottomSheet(
    title: String,
    initialValue: String,
    placeholder: String,
    confirmButtonText: String,
    allowDot: Boolean,
    validateWhileTyping: (String) -> String?,
    validateOnConfirm: (String) -> String?,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var value by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SettingsColors.Background,
        dragHandle = null,
    ) {
        ConnectionValueEditSheetContent(
            title = title,
            value = value,
            placeholder = placeholder,
            confirmButtonText = confirmButtonText,
            allowDot = allowDot,
            errorMessage = errorMessage,
            onValueChange = { newValue ->
                value = newValue
                errorMessage = validateWhileTyping(newValue)
            },
            onConfirmClick = {
                val error = validateOnConfirm(value)
                if (error != null) {
                    errorMessage = error
                } else {
                    onConfirm(value.trim())
                    onDismissRequest()
                }
            },
            onCloseClick = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpEditBottomSheet(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val errorEmpty = stringResource(R.string.settings_support_ip_error_empty)
    val errorInvalid = stringResource(R.string.settings_support_ip_error_invalid)

    ConnectionValueEditBottomSheet(
        title = stringResource(R.string.settings_support_ip_sheet_title),
        initialValue = initialValue,
        placeholder = stringResource(R.string.settings_support_ip_hint),
        confirmButtonText = stringResource(R.string.settings_support_confirm),
        allowDot = true,
        validateWhileTyping = { value ->
            when (ConnectionAddressValidator.ipError(value)) {
                ConnectionAddressValidator.IpValidationError.INVALID -> errorInvalid
                else -> null
            }
        },
        validateOnConfirm = { value ->
            when (ConnectionAddressValidator.ipError(value)) {
                ConnectionAddressValidator.IpValidationError.EMPTY -> errorEmpty
                ConnectionAddressValidator.IpValidationError.INVALID -> errorInvalid
                null -> null
            }
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortEditBottomSheet(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val errorEmpty = stringResource(R.string.settings_support_port_error_empty)
    val errorInvalid = stringResource(R.string.settings_support_port_error_invalid)

    ConnectionValueEditBottomSheet(
        title = stringResource(R.string.settings_support_port_sheet_title),
        initialValue = initialValue,
        placeholder = stringResource(R.string.settings_support_port_hint),
        confirmButtonText = stringResource(R.string.settings_support_confirm),
        allowDot = false,
        validateWhileTyping = { value ->
            when (ConnectionAddressValidator.portError(value)) {
                ConnectionAddressValidator.PortValidationError.INVALID -> errorInvalid
                else -> null
            }
        },
        validateOnConfirm = { value ->
            when (ConnectionAddressValidator.portError(value)) {
                ConnectionAddressValidator.PortValidationError.EMPTY -> errorEmpty
                ConnectionAddressValidator.PortValidationError.INVALID -> errorInvalid
                null -> null
            }
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NiiEditBottomSheet(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val errorEmpty = stringResource(R.string.settings_merchant_default_id_error_empty)
    val errorInvalid = stringResource(R.string.settings_merchant_default_id_error_invalid)

    ConnectionValueEditBottomSheet(
        title = stringResource(R.string.settings_merchant_default_id_sheet_title),
        initialValue = initialValue,
        placeholder = stringResource(R.string.settings_merchant_default_id_hint),
        confirmButtonText = stringResource(R.string.settings_support_confirm),
        allowDot = false,
        validateWhileTyping = { value ->
            when (ConnectionAddressValidator.niiError(value)) {
                ConnectionAddressValidator.NiiValidationError.INVALID -> errorInvalid
                else -> null
            }
        },
        validateOnConfirm = { value ->
            when (ConnectionAddressValidator.niiError(value)) {
                ConnectionAddressValidator.NiiValidationError.EMPTY -> errorEmpty
                ConnectionAddressValidator.NiiValidationError.INVALID -> errorInvalid
                null -> null
            }
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}
