package com.danesh.settings.navigation



import android.app.Activity

import android.util.Log

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.navigation.NavType

import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController

import androidx.navigation.navArgument

import com.danesh.common.GetPasswordScreen

import com.danesh.settings.R

import com.danesh.settings.model.AppFontFamily

import com.danesh.settings.model.AppRole

import com.danesh.settings.model.AppThemeMode

import com.danesh.settings.model.MerchantSupportLaunchRequest

import com.danesh.settings.presentation.MerchantExitPasswordViewModel

import com.danesh.settings.presentation.MerchantSettingsViewModel

import com.danesh.settings.presentation.SettingsAccessPasswordViewModel

import com.danesh.settings.navigation.SupportSettingsNavHost

import com.danesh.settings.ui.ChangePasswordRoute

import com.danesh.settings.ui.DefaultPurchaseAmountSettingsRoute

import com.danesh.settings.ui.MerchantSettingsScreen



private object SettingsRoutes {

    const val PASSWORD = "settings_password/{${SettingsNavArgs.ROLE}}"

    const val MERCHANT = "settings_merchant"


    const val DEFAULT_PURCHASE_AMOUNT = "settings_merchant_default_purchase_amount"

    const val SUPPORT = "settings_support"

    const val CHANGE_PASSWORD = "settings_change_password"

    const val CHANGE_PASSWORD_MANDATORY = "settings_change_password_mandatory"



    fun password(role: AppRole): String = "settings_password/${role.name}"

}



@Composable

fun SettingsNavHost(

    role: AppRole,

    onFlowComplete: () -> Unit,

    onLaunchSupportService: (MerchantSupportLaunchRequest) -> Unit = {},

    onExitClick:()-> Unit

) {

    val navController = rememberNavController()

    val context = LocalContext.current



    var shiftRange by rememberSaveable { mutableStateOf("8-12") }

    var selectedFont by rememberSaveable { mutableStateOf(AppFontFamily.YekanBakh.name) }

    var selectedTheme by rememberSaveable { mutableStateOf(AppThemeMode.Dark.name) }



    NavHost(

        navController = navController,

        startDestination = SettingsRoutes.password(role),

    ) {

        composable(

            route = SettingsRoutes.PASSWORD,

            arguments = listOf(

                navArgument(SettingsNavArgs.ROLE) { type = NavType.StringType },

            ),

        ) {

            val passwordViewModel: SettingsAccessPasswordViewModel = hiltViewModel()

            val passwordState by passwordViewModel.uiState.collectAsStateWithLifecycle()

            val selectedRole = it.arguments?.getString(SettingsNavArgs.ROLE)

                ?.let { name -> runCatching { AppRole.valueOf(name) }.getOrNull() }

                ?: AppRole.Merchant

            GetPasswordScreen(

                pinValue = passwordState.pinValue,

                hintText = passwordState.errorMessage,

                instructionText = role.name,

                hintColor = Color(0xFFFF8A80),

                showRetryHint = passwordState.errorMessage != null,

                onBackClick = onFlowComplete,

                onPinValueChange = passwordViewModel::onPinChange,

                onPinComplete = {

                    passwordViewModel.submitPassword(

                        onSuccess = {

                            when (selectedRole) {

                                AppRole.Merchant -> navController.navigate(SettingsRoutes.MERCHANT)

                                AppRole.Support -> navController.navigate(SettingsRoutes.SUPPORT)

                            }

                        },

                        onMandatoryPasswordChange = {

                            navController.navigate(SettingsRoutes.CHANGE_PASSWORD_MANDATORY) {

                                popUpTo(SettingsRoutes.password(selectedRole)) { inclusive = true }

                            }

                        },

                    )

                },

            )

        }



        composable(SettingsRoutes.MERCHANT) {

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

                if (merchantSettingsViewModel.requiresPasswordChange()) {

                    navController.navigate(SettingsRoutes.CHANGE_PASSWORD_MANDATORY) {

                        popUpTo(SettingsRoutes.MERCHANT) { inclusive = true }

                    }

                    return@LaunchedEffect

                }

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

                    navController.navigate(SettingsRoutes.CHANGE_PASSWORD)

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

                    navController.navigate(SettingsRoutes.DEFAULT_PURCHASE_AMOUNT)

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

             //   onThemeChange = { selectedTheme = it.name },



            )

        }



        composable(SettingsRoutes.SUPPORT) {

            SupportSettingsNavHost(

                onFlowComplete = onFlowComplete,

                onExitClick = onExitClick,

                onLaunchSupportService = onLaunchSupportService,

            )

        }



        composable(SettingsRoutes.DEFAULT_PURCHASE_AMOUNT) {

            DefaultPurchaseAmountSettingsRoute(

                onBackClick = { navController.popBackStack() },

            )

        }


        composable(SettingsRoutes.CHANGE_PASSWORD) {

            ChangePasswordRoute(

                onBackClick = { navController.popBackStack() },

                onCancelClick = onFlowComplete,

            )

        }



        composable(SettingsRoutes.CHANGE_PASSWORD_MANDATORY) {

            ChangePasswordRoute(

                mandatory = true,

                onBackClick = onFlowComplete,

                onCancelClick = onFlowComplete,

                onPasswordChanged = {

                    navController.navigate(SettingsRoutes.MERCHANT) {

                        popUpTo(SettingsRoutes.CHANGE_PASSWORD_MANDATORY) { inclusive = true }

                    }

                },

            )

        }

    }

}

