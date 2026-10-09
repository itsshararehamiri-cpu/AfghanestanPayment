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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.currency.amountInWordsWithCurrency
import com.danesh.settings.R
import com.danesh.settings.presentation.OptionalReceiptMessages
import com.danesh.settings.presentation.OptionalReceiptSettingsUiState
import com.danesh.settings.presentation.OptionalReceiptSettingsViewModel
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.textinput.AmountTransactionField
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun OptionalReceiptSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: OptionalReceiptSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = OptionalReceiptMessages(
        empty = stringResource(R.string.settings_optional_receipt_error_empty),
        invalid = stringResource(R.string.settings_optional_receipt_error_invalid),
        lowerGreaterThanUpper = stringResource(R.string.settings_optional_receipt_error_range),
        success = stringResource(R.string.settings_optional_receipt_saved),
        failed = stringResource(R.string.settings_optional_receipt_failed),
        noHostValues = stringResource(R.string.settings_optional_receipt_no_host_values),
        unsupported = stringResource(R.string.settings_optional_receipt_unsupported),
    )
    OptionalReceiptSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onLowerChange = viewModel::onLowerChange,
        onUpperChange = viewModel::onUpperChange,
        onSaveClick = { viewModel.save(messages) },
        onDismissResult = {
            val success = uiState.resultSuccess
            viewModel.clearResult()
            if (success) onBackClick()
        },
    )
}

@Composable
fun OptionalReceiptSettingsScreen(
    uiState: OptionalReceiptSettingsUiState,
    onBackClick: () -> Unit,
    onLowerChange: (String) -> Unit,
    onUpperChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissResult: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_optional_receipt_title),
            onBackClick = onBackClick,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_optional_receipt_description),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            AmountTransactionField(
                label = stringResource(R.string.settings_optional_receipt_lower),
                value = uiState.lowerInput,
                onValueChange = onLowerChange,
                placeholder = stringResource(R.string.settings_optional_receipt_lower),
                iconRes = com.danesh.ui.R.drawable.ic_coin,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.lowerError,
                amountInWords = amountInWordsWithCurrency(uiState.lowerInput),
            )
            Spacer(modifier = Modifier.height(16.dp))
            AmountTransactionField(
                label = stringResource(R.string.settings_optional_receipt_upper),
                value = uiState.upperInput,
                onValueChange = onUpperChange,
                placeholder = stringResource(R.string.settings_optional_receipt_upper),
                iconRes = com.danesh.ui.R.drawable.ic_coin,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.upperError,
                amountInWords = amountInWordsWithCurrency(uiState.upperInput),
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (uiState.inProgress) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                GradientActionButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    text = stringResource(R.string.settings_support_confirm),
                    onClick = onSaveClick,
                )
            }
        }
    }

    uiState.resultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissResult,
            text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = onDismissResult) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}
