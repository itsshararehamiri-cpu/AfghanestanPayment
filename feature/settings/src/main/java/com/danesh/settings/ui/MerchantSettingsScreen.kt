package com.danesh.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.danesh.settings.FontSelectionBottomSheet
import com.danesh.settings.LanguageSelectionBottomSheet
import com.danesh.settings.MerchantReceiptPrintSelectionBottomSheet
import com.danesh.settings.R
import com.danesh.settings.ThemeSelectionBottomSheet
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.common.receipt.MerchantReceiptPrintMode
import com.danesh.settings.model.AppFontFamily
import com.danesh.settings.model.AppLanguage
import com.danesh.settings.model.AppThemeMode
import com.danesh.settings.model.MerchantSettingsUiState
import com.danesh.settings.model.labelRes
import com.danesh.settings.ui.theme.SettingsColors
import com.danesh.ui.toolbar.Toolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantSettingsScreen(
    uiState: MerchantSettingsUiState,
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onSettlementClick: () -> Unit,
    onConnectionStatusClick: () -> Unit,
    onChangeAccountClick: () -> Unit,
    onShowFeeChange: (Boolean) -> Unit,
    onMicroPaymentIndexChange: (Boolean) -> Unit,
    onDefaultPurchaseAmountClick: () -> Unit,
    onDismissConnectionStatusDialog: () -> Unit,
    onDismissServiceUnavailableMessage: () -> Unit,
    onDismissSettlementNoTransactionsMessage: () -> Unit,
    onDismissSettlementSuccessMessage: () -> Unit,
    onResetMerchantPasswordConfirm: () -> Unit,
    onDismissMerchantPasswordResetMessage: () -> Unit,
    merchantReceiptPrintMode: MerchantReceiptPrintMode = MerchantReceiptPrintMode.OPTIONAL,
    onMerchantReceiptPrintModeChange: (MerchantReceiptPrintMode) -> Unit = {},
    shiftRange: String = "8-12",
    selectedFont: AppFontFamily = AppFontFamily.YekanBakh,
    onFontChange: (AppFontFamily) -> Unit = {},
    selectedLanguage: AppLanguage = AppLanguage.PersianDari,
    availableLanguages: List<AppLanguage> = AppLanguage.entries,
    onLanguageChange: (AppLanguage) -> Unit = {},
    selectedTheme: AppThemeMode = AppThemeMode.Dark,
    onThemeChange: (AppThemeMode) -> Unit = {},
) {
    var showFontSheet by rememberSaveable { mutableStateOf(false) }
    var showLanguageSheet by rememberSaveable { mutableStateOf(false) }
    var showThemeSheet by rememberSaveable { mutableStateOf(false) }
    var showMerchantReceiptSheet by rememberSaveable { mutableStateOf(false) }
    var showResetPasswordDialog by rememberSaveable { mutableStateOf(false) }
    val defaultMerchantPassword = SettingsPasswordRepository.DEFAULT_MERCHANT_PASSWORD

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsColors.Background),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_merchant_title),
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
                title = stringResource(R.string.settings_merchant_operations_section),
            )

            SettingsNavigationRow(
                label = stringResource(R.string.settings_merchant_settlement),
                icon = R.drawable.ic_configuration,
                iconContentDescription = stringResource(R.string.settings_merchant_settlement),
                onClick = onSettlementClick,
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_merchant_connection_status),
                icon = R.drawable.ic_connect_to_net,
                iconContentDescription = stringResource(R.string.settings_merchant_connection_status),
                valueContent = {
                    ConnectionStatusBadge(isConnected = uiState.isConnected)
                },
                onClick = onConnectionStatusClick,
            )

//            if (uiState.isChangeAccountEnabled) {
//                Spacer(modifier = Modifier.height(10.dp))
//
//                SettingsNavigationRow(
//                    label = stringResource(R.string.settings_merchant_change_account),
//                    icon = R.drawable.ic_card_tick,
//                    iconContentDescription = stringResource(R.string.settings_merchant_change_account),
//                    onClick = onChangeAccountClick,
//                )
//            }

            if (uiState.showShowFee || uiState.showMicroPaymentIndex || uiState.showDefaultPurchaseAmount) {
                Spacer(modifier = Modifier.height(20.dp))

//                if (uiState.showShowFee) {
//                    SettingsToggleRow(
//                        label = stringResource(R.string.settings_merchant_show_fee),
//                        icon = R.drawable.ic_shift,
//                        iconContentDescription = stringResource(R.string.settings_merchant_show_fee),
//                        checked = uiState.showFeeEnabled,
//                        onCheckedChange = onShowFeeChange,
//                    )
//
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//
//                if (uiState.showMicroPaymentIndex) {
//                    SettingsToggleRow(
//                        label = stringResource(R.string.settings_merchant_micro_payment_index),
//                        icon = R.drawable.ic_card_tick,
//                        iconContentDescription = stringResource(R.string.settings_merchant_micro_payment_index),
//                        checked = uiState.microPaymentIndexEnabled,
//                        onCheckedChange = onMicroPaymentIndexChange,
//                    )
//
//                    Spacer(modifier = Modifier.height(10.dp))
//                }

                if (uiState.showDefaultPurchaseAmount) {
                    SettingsNavigationRow(
                        label = stringResource(R.string.settings_merchant_default_amount),
                        icon = R.drawable.ic_card_tick,
                        iconContentDescription = stringResource(R.string.settings_merchant_default_amount),
                        value = uiState.defaultPurchaseAmountDisplay,
                        onClick = onDefaultPurchaseAmountClick,
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionTitle(
                title = stringResource(R.string.settings_device_section_title),
            )

            SettingsNavigationRow(
                label = stringResource(R.string.settings_merchant_receipt_print),
                icon = R.drawable.ic_printer,
                iconContentDescription = stringResource(R.string.settings_merchant_receipt_print),
                value = stringResource(merchantReceiptPrintMode.labelRes()),
                onClick = { showMerchantReceiptSheet = true },
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsNavigationRow(
                label = stringResource(R.string.settings_change_management_password),
                icon = R.drawable.ic_unlock,
                iconContentDescription = stringResource(R.string.settings_change_management_password),
                onClick = onChangePasswordClick,
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
                label = stringResource(R.string.settings_select_language),
                icon = R.drawable.ic_language_circle,
                iconContentDescription = stringResource(R.string.settings_select_language),
                value = stringResource(selectedLanguage.labelRes),
                onClick = { showLanguageSheet = true },
            )
        }
    }

    if (showFontSheet) {
        FontSelectionBottomSheet(
            selectedFont = selectedFont,
            onFontSelected = onFontChange,
            onDismissRequest = { showFontSheet = false },
        )
    }

    if (showLanguageSheet) {
        LanguageSelectionBottomSheet(
            selectedLanguage = selectedLanguage,
            availableLanguages = availableLanguages,
            onLanguageSelected = onLanguageChange,
            onDismissRequest = { showLanguageSheet = false },
        )
    }

    if (showThemeSheet) {
        ThemeSelectionBottomSheet(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeChange,
            onDismissRequest = { showThemeSheet = false },
        )
    }

    if (showMerchantReceiptSheet) {
        MerchantReceiptPrintSelectionBottomSheet(
            selectedMode = merchantReceiptPrintMode,
            onModeSelected = onMerchantReceiptPrintModeChange,
            onDismissRequest = { showMerchantReceiptSheet = false },
        )
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
                        onResetMerchantPasswordConfirm()
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

    uiState.merchantPasswordResetMessage?.let { password ->
        AlertDialog(
            onDismissRequest = onDismissMerchantPasswordResetMessage,
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
                TextButton(onClick = onDismissMerchantPasswordResetMessage) {
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

    if (uiState.isSettlingWithCenter) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SettingsColors.Accent)
        }
    }

    uiState.settlementNoTransactionsMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissSettlementNoTransactionsMessage,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissSettlementNoTransactionsMessage) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }

    uiState.settlementSuccessMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissSettlementSuccessMessage,
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissSettlementSuccessMessage) {
                    Text(
                        text = stringResource(R.string.settings_support_confirm),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )
    }
}

@Composable
private fun ConnectionStatusBadge(isConnected: Boolean) {
    Text(
        text = stringResource(
            if (isConnected) {
                R.string.settings_support_connected
            } else {
                R.string.settings_support_disconnected
            },
        ),
        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
        fontSize = 14.sp,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun MerchantSettingsScreenPreview() {
    MerchantSettingsScreen(
        uiState = MerchantSettingsUiState(
            isChangeAccountEnabled = true,
            showFeeEnabled = true,
        ),
        onBackClick = {},
        onChangePasswordClick = {},
        onSettlementClick = {},
        onConnectionStatusClick = {},
        onChangeAccountClick = {},
        onShowFeeChange = {},
        onMicroPaymentIndexChange = {},
        onDefaultPurchaseAmountClick = {},
        onDismissConnectionStatusDialog = {},
        onDismissServiceUnavailableMessage = {},
        onDismissSettlementNoTransactionsMessage = {},
        onDismissSettlementSuccessMessage = {},
        onResetMerchantPasswordConfirm = {},
        onDismissMerchantPasswordResetMessage = {},
    )
}
