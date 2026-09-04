package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.R
import com.danesh.settings.model.MerchantSupportLaunchRequest
import com.danesh.settings.presentation.SupportSettingsViewModel
import com.danesh.common.network.openNetworkSettings

@Composable
fun SupportSettingsRoute(
    onBackClick: () -> Unit,
    onConfigurationClick: () -> Unit,
    onMainServerClick: () -> Unit,
    onDefaultIdClick: () -> Unit,
    onBackupPlatformClick: () -> Unit,
    onMenuFeaturesClick: () -> Unit,
    onSupportServicesClick: () -> Unit,
    onVatPercentageClick: () -> Unit,
    onMicroPaymentIndexClick: () -> Unit,
    onExitClick: () -> Unit,
    onLaunchSupportService: (MerchantSupportLaunchRequest) -> Unit = {},
    viewModel: SupportSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val serviceUnavailableMessage = stringResource(R.string.settings_merchant_service_unavailable)
    val tmUpdateServerNotConfiguredMessage =
        stringResource(R.string.settings_merchant_tm_update_server_not_configured)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNetworkStatus()
                viewModel.refreshVatPercentage()
                viewModel.refreshMicroPaymentIndexAmount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.launchSupportService.collect { request ->
            onLaunchSupportService(request)
        }
    }

    SupportSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onInternetClick = { openNetworkSettings(context) },
        onConnectionStatusClick = viewModel::onConnectionStatusClick,
        onDismissConnectionStatusDialog = viewModel::dismissConnectionStatusDialog,
        onMainServerClick = onMainServerClick,
        onDefaultIdClick = onDefaultIdClick,
        onBackupPlatformClick = onBackupPlatformClick,
        onShowFeeChange = viewModel::setShowFeeEnabled,
        onConfigurationClick =
            onConfigurationClick
        ,
        onMenuFeaturesClick = onMenuFeaturesClick,
        onSupportServicesClick = onSupportServicesClick,
        onVatPercentageClick = onVatPercentageClick,
        onMicroPaymentIndexClick = onMicroPaymentIndexClick,
        onResetMerchantPassword = viewModel::resetMerchantPassword,
        onTerminalReplacement = viewModel::replaceTerminal,
        onTmUpdateClick = {
            viewModel.onTmUpdateClick(
                unavailableMessage = serviceUnavailableMessage,
                serverNotConfiguredMessage = tmUpdateServerNotConfiguredMessage,
            )
        },
        onExitClick = onExitClick,
        merchantPasswordResetMessage = uiState.merchantPasswordResetMessage,
        onDismissResetMessage = viewModel::clearMerchantPasswordResetMessage,
        terminalReplacementSuccess = uiState.terminalReplacementSuccess,
        onDismissTerminalReplacementSuccess = viewModel::clearTerminalReplacementSuccess,
        onDismissTmUpdateServerNotConfiguredMessage =
            viewModel::clearTmUpdateServerNotConfiguredMessage,
        onDismissServiceUnavailableMessage = viewModel::clearServiceUnavailableMessage,
        onTerminalClick = {},
    )
}
