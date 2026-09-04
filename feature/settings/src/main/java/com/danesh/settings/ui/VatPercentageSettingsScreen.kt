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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.settings.model.VatPercentageSettingsUiState
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun VatPercentageSettingsScreen(
    uiState: VatPercentageSettingsUiState,
    onBackClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissSavedMessage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_vat_percentage_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsSectionTitle(
                title = stringResource(R.string.settings_support_vat_percentage_section),
            )

            Text(
                text = stringResource(R.string.settings_support_vat_percentage_description),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsTextField(
                value = uiState.inputValue,
                onValueChange = onInputChange,
                placeholder = stringResource(R.string.settings_support_vat_percentage_hint),
                errorMessage = uiState.errorMessage,
                keyboardType = KeyboardType.Number,
            )

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

    uiState.savedMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissSavedMessage,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissSavedMessage) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun VatPercentageSettingsScreenPreview() {
    VatPercentageSettingsScreen(
        uiState = VatPercentageSettingsUiState(
            vatPercentage = "10",
            inputValue = "10",
        ),
        onBackClick = {},
        onInputChange = {},
        onSaveClick = {},
        onDismissSavedMessage = {},
    )
}
