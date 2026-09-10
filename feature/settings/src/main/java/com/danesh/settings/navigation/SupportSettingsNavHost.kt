package com.danesh.settings.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.danesh.common.GetPasswordScreen
import com.danesh.common.SwipeCardScreen
import com.danesh.common.presentation.viewmodel.SwipeCardViewModel
import com.danesh.settings.R
import com.danesh.settings.model.AppRole
import com.danesh.settings.model.MerchantSupportLaunchRequest
import com.danesh.settings.presentation.ConfigurationViewModel
import com.danesh.settings.presentation.InitialConfigurationViewModel
import com.danesh.settings.presentation.KeyLoadingViewModel
import com.danesh.settings.presentation.MerchantExitPasswordViewModel
import com.danesh.settings.presentation.SadadKeyCardLoadingViewModel
import com.danesh.settings.presentation.SupportServicesFlowViewModel
import com.danesh.settings.presentation.SupportSettingsViewModel
import com.danesh.settings.presentation.TerminalConfigViewModel
import com.danesh.support.SupportScreen
import com.danesh.support.presentation.SupportViewModel
import com.danesh.settings.ui.ConfigurationScreen
import com.danesh.settings.ui.KeyLoadingScreen
import com.danesh.settings.ui.SadadKeyCardLoadingScreen
import com.danesh.settings.ui.TerminalSetupScreen
import com.danesh.settings.ui.BackupPlatformRoute
import com.danesh.settings.ui.DefaultIdSettingsRoute
import com.danesh.settings.ui.MainServerSettingsRoute
import com.danesh.settings.ui.MenuFeatureSettingsRoute
import com.danesh.settings.ui.SupportSettingsRoute
import com.danesh.settings.ui.SupportSettingsRouteNonBp
import com.danesh.settings.ui.MicroPaymentIndexSettingsRoute
import com.danesh.settings.ui.VatPercentageSettingsRoute
import com.danesh.settings.ui.WifiSelectionRoute

private object SupportSettingsRoutes {
    const val MAIN = "support_settings_main"
    const val CONFIGURATION = "support_settings_configuration"
    const val KEY_LOADING = "support_settings_key_loading"
    const val INITIAL_CONFIGURATION = "support_settings_initial_configuration"
    const val TERMINAL_CONFIG = "support_settings_terminal_config"
    const val MENU_FEATURES = "support_settings_menu_features"
    const val SUPPORT_SERVICES_FLOW = "support_settings_services_flow"
    const val VAT_PERCENTAGE = "support_settings_vat_percentage"
    const val MICRO_PAYMENT_INDEX = "support_settings_micro_payment_index"
    const val EXIT_PASSWORD = "support_settings_exit_password"
    const val MAIN_SERVER = "support_settings_main_server"
    const val WIFI_SELECTION = "support_settings_wifi_selection"
    const val DEFAULT_ID = "support_settings_default_id"
    const val BACKUP_PLATFORM = "support_settings_backup_platform"
}

private object SupportServicesFlowRoutes {
    const val CARD = "support_settings_services_card"
    const val LIST = "support_settings_services_list"
}

@Composable
fun SupportSettingsNavHost(
    onFlowComplete: () -> Unit,
    onExitClick: () -> Unit,
    onLaunchSupportService: (MerchantSupportLaunchRequest) -> Unit = {},
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SupportSettingsRoutes.MAIN,
    ) {
        composable(SupportSettingsRoutes.MAIN) {
            val supportSettingsViewModel: SupportSettingsViewModel = hiltViewModel()
            val mainUiState by supportSettingsViewModel.uiState.collectAsStateWithLifecycle()

            if (mainUiState.usesSimplifiedSupportSettings) {
                SupportSettingsRouteNonBp(
                    viewModel = supportSettingsViewModel,
                    onBackClick = onFlowComplete,
                    onMainServerClick = {
                        navController.navigate(SupportSettingsRoutes.MAIN_SERVER)
                    },
                    onConfigurationClick = {
                        navController.navigate(SupportSettingsRoutes.CONFIGURATION)
                    },
                    onMenuFeaturesClick = {
                        navController.navigate(SupportSettingsRoutes.MENU_FEATURES)
                    },
                    onExitClick = {
                        navController.navigate(SupportSettingsRoutes.EXIT_PASSWORD)
                    },
                )
            } else {
                SupportSettingsRoute(
                    viewModel = supportSettingsViewModel,
                    onBackClick = onFlowComplete,
                    onMainServerClick = {
                        navController.navigate(SupportSettingsRoutes.MAIN_SERVER)
                    },
                    onDefaultIdClick = {
                        navController.navigate(SupportSettingsRoutes.DEFAULT_ID)
                    },
                    onBackupPlatformClick = {
                        navController.navigate(SupportSettingsRoutes.BACKUP_PLATFORM)
                    },
                    onConfigurationClick = {
                        navController.navigate(SupportSettingsRoutes.CONFIGURATION)
                    },
                    onMenuFeaturesClick = {
                        navController.navigate(SupportSettingsRoutes.MENU_FEATURES)
                    },
                    onSupportServicesClick = {
                        navController.navigate(SupportSettingsRoutes.SUPPORT_SERVICES_FLOW)
                    },
                    onVatPercentageClick = {
                        navController.navigate(SupportSettingsRoutes.VAT_PERCENTAGE)
                    },
                    onMicroPaymentIndexClick = {
                        navController.navigate(SupportSettingsRoutes.MICRO_PAYMENT_INDEX)
                    },
                    onExitClick = {
                        navController.navigate(SupportSettingsRoutes.EXIT_PASSWORD)
                    },
                    onLaunchSupportService = onLaunchSupportService,
                )
            }
        }

        navigation(
            route = SupportSettingsRoutes.SUPPORT_SERVICES_FLOW,
            startDestination = SupportServicesFlowRoutes.CARD,
        ) {
            composable(SupportServicesFlowRoutes.CARD) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SupportSettingsRoutes.SUPPORT_SERVICES_FLOW)
                }
                val flowViewModel: SupportServicesFlowViewModel = hiltViewModel(parentEntry)
                val swipeCardViewModel: SwipeCardViewModel = hiltViewModel()
                SwipeCardScreen(
                    viewModel = swipeCardViewModel,
                    onBackClick = {
                        flowViewModel.clear()
                        navController.popBackStack()
                    },
                    onCardRead = { track2, pan ->
                        flowViewModel.onCardVerified(track2, pan)
                        navController.navigate(SupportServicesFlowRoutes.LIST)
                    },
                    onTimeout = {
                        flowViewModel.clear()
                        navController.popBackStack()
                    },
                    cancelReading = swipeCardViewModel::clearCardData,
                )
            }

            composable(SupportServicesFlowRoutes.LIST) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SupportSettingsRoutes.SUPPORT_SERVICES_FLOW)
                }
                val flowViewModel: SupportServicesFlowViewModel = hiltViewModel(parentEntry)
                val supportViewModel: SupportViewModel = hiltViewModel()
                SupportScreen(
                    viewModel = supportViewModel,
                    onBackClick = {
                        flowViewModel.clear()
                        navController.popBackStack(
                            SupportSettingsRoutes.SUPPORT_SERVICES_FLOW,
                            inclusive = true,
                        )
                    },
                    onConfirmClick = { serviceId, amount, title ->
                        onLaunchSupportService(
                            MerchantSupportLaunchRequest(
                                amount = amount,
                                serviceId = serviceId,
                                title = title,
                                track2 = flowViewModel.verifiedTrack2,
                                pan = flowViewModel.verifiedPan,
                            ),
                        )
                    },
                )
            }
        }

        composable(SupportSettingsRoutes.EXIT_PASSWORD) {
            val context = LocalContext.current
            val exitPasswordViewModel: MerchantExitPasswordViewModel = hiltViewModel()
            val exitPasswordState by exitPasswordViewModel.uiState.collectAsStateWithLifecycle()

            GetPasswordScreen(
                title = stringResource(R.string.settings_merchant_exit),
                pinValue = exitPasswordState.pinValue,
                hintText = exitPasswordState.errorMessage,
                instructionText = AppRole.Support.name,
                hintColor = Color(0xFFFF8A80),
                showRetryHint = exitPasswordState.errorMessage != null,
                onBackClick = { navController.popBackStack() },
                onPinValueChange = exitPasswordViewModel::onPinChange,
                onPinComplete = {
                    exitPasswordViewModel.submitPassword {
                        (context as? Activity)?.finishAffinity()
                    }
                },
            )
        }

        composable(SupportSettingsRoutes.VAT_PERCENTAGE) {
            VatPercentageSettingsRoute(
                onBackClick = {
                    navController.popBackStack(SupportSettingsRoutes.MAIN, inclusive = false)
                },
            )
        }

        composable(SupportSettingsRoutes.MICRO_PAYMENT_INDEX) {
            MicroPaymentIndexSettingsRoute(
                onBackClick = {
                    navController.popBackStack(SupportSettingsRoutes.MAIN, inclusive = false)
                },
            )
        }

        composable(SupportSettingsRoutes.MENU_FEATURES) {
            MenuFeatureSettingsRoute(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(SupportSettingsRoutes.CONFIGURATION) {
            val configurationViewModel: ConfigurationViewModel = hiltViewModel()
            val usesTerminalConfigFlow = configurationViewModel.usesTerminalConfigFlow

            if (usesTerminalConfigFlow) {
                // همراه‌پی: «کلیدگذاری» بدون رفتن به صفحه بعد، همین‌جا اجرا و نتیجه‌اش نمایش داده می‌شود.
                val keyingViewModel: InitialConfigurationViewModel = hiltViewModel()
                val keyingUiState by keyingViewModel.uiState.collectAsStateWithLifecycle()

                ConfigurationScreen(
                    onBackClick = { navController.popBackStack() },
                    keyLoadingLabel = stringResource(R.string.settings_key_provisioning),
                    initialConfigurationLabel = stringResource(R.string.settings_support_configuration),
                    onKeyLoadingClick = keyingViewModel::confirm,
                    onInitialConfigurationClick = {
                        navController.navigate(SupportSettingsRoutes.TERMINAL_CONFIG)
                    },
                    keyingIsLoading = keyingUiState.isLoading,
                    keyingResultMessage = keyingUiState.resultMessage
                        ?: keyingUiState.summary?.let {
                            stringResource(R.string.settings_key_provisioning_success)
                        },
                    onDismissKeyingResult = keyingViewModel::clearKeyingResult,
                )
            } else {
                ConfigurationScreen(
                    onBackClick = { navController.popBackStack() },
                    onKeyLoadingClick = {
                        navController.navigate(SupportSettingsRoutes.KEY_LOADING)
                    },
                    onInitialConfigurationClick = {
                        navController.navigate(SupportSettingsRoutes.INITIAL_CONFIGURATION)
                    },
                )
            }
        }

        composable(SupportSettingsRoutes.TERMINAL_CONFIG) {
            val viewModel: TerminalConfigViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TerminalSetupScreen(
                uiState = uiState,
                title = stringResource(R.string.settings_support_configuration),
                onBackClick = { navController.popBackStack() },
                onFirstBallotTicketChange = {},
                onSecondBallotTicketChange = {},
                onScanFirstBallotTicket = {},
                onScanSecondBallotTicket = {},
                onConfirmClick = viewModel::confirm,
                onCancelClick = { navController.popBackStack() },
                onSummaryConfirm = {
                    viewModel.dismissSummary()
                    navController.popBackStack(SupportSettingsRoutes.CONFIGURATION, inclusive = false)
                },
                onPrintClick = {},
                onExecuteClick = {},
            )
        }

        composable(SupportSettingsRoutes.KEY_LOADING) {
            val viewModel: KeyLoadingViewModel = hiltViewModel()

            if (viewModel.usesKeyCardLoading) {
                val cardViewModel: SadadKeyCardLoadingViewModel = hiltViewModel()
                val cardUiState by cardViewModel.uiState.collectAsStateWithLifecycle()

                SadadKeyCardLoadingScreen(
                    uiState = cardUiState,
                    onBackClick = { navController.popBackStack() },
                    onKeyIndexChange = cardViewModel::updateKeyIndex,
                    onCardAPinChange = cardViewModel::updateCardAPin,
                    onCardBcPinChange = cardViewModel::updateCardBcPin,
                    onSelectCard = cardViewModel::selectCard,
                    onReadCardA = cardViewModel::readCardA,
                    onReadCardBc = cardViewModel::readCardBOrC,
                )
            } else {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val title = stringResource(R.string.settings_key_loading)

                KeyLoadingScreen(
                    title = title,
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onFirstBallotTicketChange = viewModel::updateFirstBallotTicket,
                    onSecondBallotTicketChange = viewModel::updateSecondBallotTicket,
                    onScanFirstBallotTicket = viewModel::scanFirstBallotTicket,
                    onScanSecondBallotTicket = viewModel::scanSecondBallotTicket,
                    onConfirmClick = viewModel::confirm,
                    onCancelClick = { navController.popBackStack() },
                )
            }
        }

        composable(SupportSettingsRoutes.INITIAL_CONFIGURATION) {
            val viewModel: InitialConfigurationViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val configurationViewModel: ConfigurationViewModel = hiltViewModel()
            val initialConfigTitle = if (configurationViewModel.usesTerminalConfigFlow) {
                stringResource(R.string.settings_key_provisioning)
            } else {
                stringResource(R.string.settings_terminal_setup)
            }

            TerminalSetupScreen(
                uiState = uiState,
                title = initialConfigTitle,
                onBackClick = {
                    if (viewModel.onBackFromSetup()) {
                        navController.popBackStack()
                    }
                },
                onFirstBallotTicketChange = viewModel::updateFirstBallotTicket,
                onSecondBallotTicketChange = viewModel::updateSecondBallotTicket,
                onScanFirstBallotTicket = viewModel::scanFirstBallotTicket,
                onScanSecondBallotTicket = viewModel::scanSecondBallotTicket,
                onConfirmClick = viewModel::confirm,
                onCancelClick = {
                    if (viewModel.onBackFromSetup()) {
                        navController.popBackStack()
                    }
                },
                onSummaryConfirm = {
                    viewModel.dismissSummary()
                    navController.popBackStack(SupportSettingsRoutes.CONFIGURATION, inclusive = false)
                },
                onPrintClick = viewModel::printConfigurationReceiptOnly,
                onExecuteClick = viewModel::selectExecute,
            )
        }

        composable(SupportSettingsRoutes.MAIN_SERVER) {
            MainServerSettingsRoute(
                onBackClick = { navController.popBackStack() },
                onNavigateToWifiSelection = {
                    navController.navigate(SupportSettingsRoutes.WIFI_SELECTION)
                },
            )
        }

        composable(SupportSettingsRoutes.WIFI_SELECTION) {
            WifiSelectionRoute(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(SupportSettingsRoutes.DEFAULT_ID) {
            DefaultIdSettingsRoute(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(SupportSettingsRoutes.BACKUP_PLATFORM) {
            BackupPlatformRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
