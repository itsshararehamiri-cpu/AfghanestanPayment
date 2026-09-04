package com.danesh.settings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danesh.settings.R
import com.danesh.settings.model.AppFontFamily
import com.danesh.settings.model.AppThemeMode
import com.danesh.settings.model.MerchantSupportLaunchRequest
import com.danesh.settings.presentation.MerchantSettingsViewModel
import com.danesh.settings.ui.ChangePasswordRoute
import com.danesh.settings.ui.DefaultPurchaseAmountSettingsRoute
import com.danesh.settings.ui.MerchantSettingsScreen

private object MerchantSettingsRoutes {
    const val SETTINGS = "merchant_settings"
    const val CHANGE_PASSWORD = "merchant_change_password"
    const val DEFAULT_PURCHASE_AMOUNT = "merchant_default_purchase_amount"
}

@Composable
fun MerchantSettingsNavHost(
    onFlowComplete: () -> Unit,
    onLaunchSupportService: (MerchantSupportLaunchRequest) -> Unit = {},
) {
    val navController = rememberNavController()

    var shiftRange by rememberSaveable { mutableStateOf("8-12") }
    var selectedFont by rememberSaveable { mutableStateOf(AppFontFamily.YekanBakh.name) }
    var selectedTheme by rememberSaveable { mutableStateOf(AppThemeMode.Dark.name) }

    NavHost(
        navController = navController,
        startDestination = MerchantSettingsRoutes.SETTINGS,
    ) {
        composable(MerchantSettingsRoutes.SETTINGS) {
            val merchantSettingsViewModel: MerchantSettingsViewModel = hiltViewModel()
            val selectedLanguage by merchantSettingsViewModel.selectedLanguage.collectAsStateWithLifecycle()
            val merchantReceiptPrintMode by merchantSettingsViewModel.merchantReceiptPrintMode.collectAsStateWithLifecycle()
            val merchantUiState by merchantSettingsViewModel.uiState.collectAsStateWithLifecycle()
            val availableLanguages = merchantSettingsViewModel.availableLanguages
            val serviceUnavailableMessage = stringResource(
                R.string.settings_merchant_service_unavailable,
            )
            val settlementNoTransactionsMessage = stringResource(
                R.string.settings_merchant_settlement_no_transactions,
            )
            val settlementSuccessMessage = stringResource(
                R.string.settings_merchant_settlement_success,
            )

            LaunchedEffect(merchantSettingsViewModel) {
                merchantSettingsViewModel.launchSupportService.collect { request ->
                    onLaunchSupportService(request)
                }
            }

            androidx.compose.runtime.DisposableEffect(Unit) {
                merchantSettingsViewModel.refreshMerchantDisplayPrefs()
                onDispose { }
            }

            MerchantSettingsScreen(
                uiState = merchantUiState,
                onBackClick = onFlowComplete,
                onChangePasswordClick = {
                    navController.navigate(MerchantSettingsRoutes.CHANGE_PASSWORD)
                },
                onSettlementClick = {
                    merchantSettingsViewModel.onSettlementClick(
                        settlementNoTransactionsMessage,
                        settlementSuccessMessage,
                    )
                },
                onConnectionStatusClick = merchantSettingsViewModel::onConnectionStatusClick,
                onChangeAccountClick = {
                    merchantSettingsViewModel.onChangeAccountClick(serviceUnavailableMessage)
                },
                onShowFeeChange = merchantSettingsViewModel::setShowFeeEnabled,
                onMicroPaymentIndexChange = merchantSettingsViewModel::setMicroPaymentIndexEnabled,
                onDefaultPurchaseAmountClick = {
                    navController.navigate(MerchantSettingsRoutes.DEFAULT_PURCHASE_AMOUNT)
                },
                onDismissConnectionStatusDialog = merchantSettingsViewModel::dismissConnectionStatusDialog,
                onDismissServiceUnavailableMessage = merchantSettingsViewModel::clearServiceUnavailableMessage,
                onDismissSettlementNoTransactionsMessage =
                    merchantSettingsViewModel::clearSettlementNoTransactionsMessage,
                onDismissSettlementSuccessMessage =
                    merchantSettingsViewModel::clearSettlementSuccessMessage,
                onResetMerchantPasswordConfirm = merchantSettingsViewModel::resetMerchantPassword,
                onDismissMerchantPasswordResetMessage = merchantSettingsViewModel::clearMerchantPasswordResetMessage,
                merchantReceiptPrintMode = merchantReceiptPrintMode,
                onMerchantReceiptPrintModeChange = merchantSettingsViewModel::setMerchantReceiptPrintMode,
                shiftRange = shiftRange,
                selectedFont = AppFontFamily.valueOf(selectedFont),
                onFontChange = { selectedFont = it.name },
                selectedLanguage = selectedLanguage,
                availableLanguages = availableLanguages,
                onLanguageChange = merchantSettingsViewModel::setLanguage,
                selectedTheme = AppThemeMode.valueOf(selectedTheme),
                onThemeChange = { selectedTheme = it.name },
            )
        }
        composable(MerchantSettingsRoutes.DEFAULT_PURCHASE_AMOUNT) {
            DefaultPurchaseAmountSettingsRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(MerchantSettingsRoutes.CHANGE_PASSWORD) {
            ChangePasswordRoute(
                onBackClick = { navController.popBackStack() },
                onCancelClick = onFlowComplete,
            )
        }
    }
}
