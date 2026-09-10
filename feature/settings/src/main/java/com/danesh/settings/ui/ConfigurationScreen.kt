package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.settings.R
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun ConfigurationScreen(
    onBackClick: () -> Unit,
    onKeyLoadingClick: () -> Unit = {},
    onInitialConfigurationClick: () -> Unit,
    keyLoadingLabel: String = stringResource(R.string.settings_key_loading),
    initialConfigurationLabel: String = stringResource(R.string.settings_initial_configuration),
    keyingIsLoading: Boolean = false,
    keyingResultMessage: String? = null,
    onDismissKeyingResult: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_configuration_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
//            SettingsSectionTitle(
//                title = stringResource(R.string.settings_configuration_section_title),
//            )

            SettingsNavigationRow(
                label = keyLoadingLabel,
                icon = R.drawable.ic_unlock,
                iconContentDescription = keyLoadingLabel,
                isLoading = keyingIsLoading,
                onClick = onKeyLoadingClick,
            )

            if (keyingIsLoading) {
                Spacer(modifier = Modifier.height(10.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = initialConfigurationLabel,
                icon = R.drawable.ic_configuration,
                iconContentDescription = initialConfigurationLabel,
                onClick = onInitialConfigurationClick,
            )
        }
    }

    if (keyingResultMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissKeyingResult,
            text = {
                Text(
                    text = keyingResultMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissKeyingResult) {
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
private fun ConfigurationScreenPreview() {
    ConfigurationScreen(
        onBackClick = {},
        onInitialConfigurationClick = {},
    )
}
