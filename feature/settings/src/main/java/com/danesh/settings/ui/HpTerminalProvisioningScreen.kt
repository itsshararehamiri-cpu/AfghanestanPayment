package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.model.HpTerminalProvisioningUiState
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

/**
 * صفحه «پیکربندی پایانه» همراه‌پی — «کلیدگذاری» و «دریافت اطلاعات پایانه» هر دو
 * در همین صفحه انجام می‌شوند؛ با زدن هر کدام صفحه عوض نمی‌شود و نتیجه (و در صورت
 * موفقیت، KCV یا مشخصات پایانه) همان‌جا زیر دکمه نمایش داده می‌شود.
 */
@Composable
fun HpTerminalProvisioningScreen(
    uiState: HpTerminalProvisioningUiState,
    onBackClick: () -> Unit,
    onKeyLoadingClick: () -> Unit,
    onFetchTerminalInfoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_configuration),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsSectionTitle(title = stringResource(R.string.settings_key_provisioning))

            GradientActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_key_provisioning),
                onClick = { if (!uiState.keyLoading.isLoading) onKeyLoadingClick() },
            )

            if (uiState.keyLoading.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = SettingsColors.Accent,
                )
            }

            uiState.keyLoading.resultMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = if (uiState.keyLoading.isSuccess) SettingsColors.Accent else SettingsColors.TextPrimary,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            uiState.keyLoading.kcvSummary?.let { kcvSummary ->
                Spacer(modifier = Modifier.height(16.dp))
                KeyLoadingKcvTable(kcvSummary = kcvSummary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_support_configuration))

            GradientActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_terminal_info_fetch),
                onClick = { if (!uiState.terminalInfo.isLoading) onFetchTerminalInfoClick() },
            )

            if (uiState.terminalInfo.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = SettingsColors.Accent,
                )
            }

            uiState.terminalInfo.resultMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = if (uiState.terminalInfo.isSuccess) SettingsColors.Accent else SettingsColors.TextPrimary,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            uiState.terminalInfo.summary?.let { summary ->
                Spacer(modifier = Modifier.height(16.dp))
                ConfigurationSummaryTable(summary = summary)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun HpTerminalProvisioningScreenPreview() {
    HpTerminalProvisioningScreen(
        uiState = HpTerminalProvisioningUiState(),
        onBackClick = {},
        onKeyLoadingClick = {},
        onFetchTerminalInfoClick = {},
    )
}
