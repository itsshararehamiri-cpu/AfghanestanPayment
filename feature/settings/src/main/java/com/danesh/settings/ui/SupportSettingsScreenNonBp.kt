package com.danesh.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.settings.R
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.model.SupportSettingsUiState
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

/**
 * صفحه تنظیمات پشتیبانی همراه‌پی و سداد — نسخه‌ای مستقل از [SupportSettingsScreen] (به‌پرداخت)
 * تا تغییرات مخصوص این دو PSP بدون اثر روی به‌پرداخت اعمال شود. گزینه «خدمات پشتیبانی» و
 * ردیف‌های مخصوص به‌پرداخت (تخفیف درصدی، سهمیه ریز‌پرداخت، سرور TMS) در این صفحه اصلاً نمایش
 * داده نمی‌شوند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSettingsScreenNonBp(
    uiState: SupportSettingsUiState = SupportSettingsUiState(),
    onBackClick: () -> Unit,
    onInternetClick: () -> Unit,
    onConnectionStatusClick: () -> Unit,
    onDismissConnectionStatusDialog: () -> Unit,
    onMainServerClick: () -> Unit,
    onConfigurationClick: () -> Unit,
    onMenuFeaturesClick: () -> Unit,
    onResetMerchantPassword: () -> Unit,
    onTerminalReplacement: () -> Unit,
    onExitClick: () -> Unit,
    merchantPasswordResetMessage: String? = null,
    onDismissResetMessage: () -> Unit = {},
    terminalReplacementSuccess: Boolean = false,
    onDismissTerminalReplacementSuccess: () -> Unit = {},
    onDismissServiceUnavailableMessage: () -> Unit = {},
    onKeyLoadingClick: () -> Unit = {},
    onDismissKeyLoadingResult: () -> Unit = {},
    onDismissKeyLoadingError: () -> Unit = {},
    onFetchTerminalInfoClick: () -> Unit = {},
    onDismissTerminalInfoResult: () -> Unit = {},
    onDismissTerminalInfoError: () -> Unit = {},
) {
    var showResetPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showTerminalReplacementDialog by rememberSaveable { mutableStateOf(false) }
    val defaultMerchantPassword = SettingsPasswordRepository.DEFAULT_MERCHANT_PASSWORD
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_title),
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
                title = stringResource(R.string.settings_section_title),
            )

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_internet),
                icon = R.drawable.ic_connect_to_net,
                iconContentDescription = stringResource(R.string.settings_support_internet),
                valueContent = {
                    ConnectionStatusBadgeNonBp(isConnected = uiState.isConnected)
                },
                onClick = onInternetClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_merchant_connection_status),
                icon = R.drawable.ic_connect_to_net,
                iconContentDescription = stringResource(R.string.settings_merchant_connection_status),
                onClick = onConnectionStatusClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_main_server_title),
                icon = R.drawable.ic_ip,
                iconContentDescription = stringResource(R.string.settings_support_main_server_title),
                onClick = onMainServerClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.usesTerminalConfigFlow) {
                SettingsNavigationRow(
                    label = stringResource(R.string.settings_key_provisioning),
                    icon = R.drawable.ic_unlock,
                    iconContentDescription = stringResource(R.string.settings_key_provisioning),
                    onClick = { if (!uiState.isKeyLoadingInProgress) onKeyLoadingClick() },
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_configuration),
                icon = R.drawable.ic_configuration,
                iconContentDescription = stringResource(R.string.settings_support_configuration),
                value = uiState.configuration,
                valueColor = SettingsColors.TextPrimary,
                onClick = {
                    if (uiState.usesTerminalConfigFlow) {
                        if (!uiState.isTerminalInfoInProgress) onFetchTerminalInfoClick()
                    } else {
                        onConfigurationClick()
                    }
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_menu_features),
                icon = R.drawable.ic_card_tick,
                iconContentDescription = stringResource(R.string.settings_support_menu_features),
                onClick = onMenuFeaturesClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_reset_merchant_password),
                icon = R.drawable.ic_unlock,
                iconContentDescription = stringResource(R.string.settings_support_reset_merchant_password),
                onClick = { showResetPasswordDialog = true },
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_support_terminal_replacement),
                icon = R.drawable.ic_terminal_info,
                iconContentDescription = stringResource(R.string.settings_support_terminal_replacement),
                onClick = { showTerminalReplacementDialog = true },
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_merchant_exit),
                icon = R.drawable.ic_unlock,
                iconContentDescription = stringResource(R.string.settings_merchant_exit),
                labelColor = Color(0xFFFF5252),
                onClick = onExitClick,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_support_reset_merchant_password),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.settings_support_reset_merchant_password_message,
                        defaultMerchantPassword,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetPasswordDialog = false
                        onResetMerchantPassword()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text(
                        text = stringResource(R.string.settings_cancel),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    if (showTerminalReplacementDialog) {
        AlertDialog(
            onDismissRequest = { showTerminalReplacementDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_support_terminal_replacement),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_support_terminal_replacement_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTerminalReplacementDialog = false
                        onTerminalReplacement()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminalReplacementDialog = false }) {
                    Text(
                        text = stringResource(R.string.settings_cancel),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    merchantPasswordResetMessage?.let { password ->
        AlertDialog(
            onDismissRequest = onDismissResetMessage,
            text = {
                Text(
                    text = stringResource(
                        R.string.settings_support_reset_merchant_password_success,
                        password,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissResetMessage) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    if (terminalReplacementSuccess) {
        AlertDialog(
            onDismissRequest = onDismissTerminalReplacementSuccess,
            text = {
                Text(
                    text = stringResource(R.string.settings_support_terminal_replacement_success),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissTerminalReplacementSuccess) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    uiState.serviceUnavailableMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissServiceUnavailableMessage,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissServiceUnavailableMessage) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    if (uiState.showConnectionStatusDialog) {
        ConnectionStatusCheckDialog(
            connectionType = uiState.connectionType,
            isChecking = uiState.isCheckingConnectionStatus,
            isServerConnected = uiState.isServerConnected,
            onDismiss = onDismissConnectionStatusDialog,
        )
    }

    uiState.keyLoadingErrorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissKeyLoadingError,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissKeyLoadingError) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    uiState.keyLoadingKcvSummary?.let { kcvSummary ->
        AlertDialog(
            onDismissRequest = onDismissKeyLoadingResult,
            title = {
                DialogTitleWithCloseButton(
                    title = stringResource(R.string.settings_key_loading_kcv_title),
                    onCloseClick = onDismissKeyLoadingResult,
                )
            },
            text = {
                KeyLoadingKcvTable(kcvSummary = kcvSummary)
            },
            confirmButton = {},
        )
    }

    uiState.terminalInfoErrorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissTerminalInfoError,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissTerminalInfoError) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    uiState.terminalInfoSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = onDismissTerminalInfoResult,
            title = {
                DialogTitleWithCloseButton(
                    title = stringResource(R.string.settings_initial_configuration_summary_title),
                    onCloseClick = onDismissTerminalInfoResult,
                )
            },
            text = {
                ConfigurationSummaryTable(summary = summary)
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun DialogTitleWithCloseButton(
    title: String,
    onCloseClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.settings_close),
            )
        }
    }
}

@Composable
private fun ConnectionStatusBadgeNonBp(isConnected: Boolean) {
    Text(
        text = stringResource(
            if (isConnected) {
                R.string.settings_support_connected
            } else {
                R.string.settings_support_disconnected
            },
        ),
        color = if (isConnected) {
            Color(0xFF4CAF50)
        } else {
            Color(0xFFFF5252)
        },
        fontSize = 14.sp,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun SupportSettingsScreenNonBpPreview() {
    SupportSettingsScreenNonBp(
        onBackClick = {},
        onInternetClick = {},
        onConnectionStatusClick = {},
        onDismissConnectionStatusDialog = {},
        onMainServerClick = {},
        onConfigurationClick = {},
        onMenuFeaturesClick = {},
        onResetMerchantPassword = {},
        onTerminalReplacement = {},
        onExitClick = {},
    )
}
