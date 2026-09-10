package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.common.network.openNetworkSettings
import com.danesh.settings.presentation.SupportSettingsViewModel

/**
 * نسخه همراه‌پی و سداد از [SupportSettingsRoute] — همان [SupportSettingsViewModel] مشترک را
 * به [SupportSettingsScreenNonBp] وصل می‌کند (بدون گزینه «خدمات پشتیبانی»).
 */
@Composable
fun SupportSettingsRouteNonBp(
    onBackClick: () -> Unit,
    onConfigurationClick: () -> Unit,
    onMainServerClick: () -> Unit,
    onMenuFeaturesClick: () -> Unit,
    onExitClick: () -> Unit,
    viewModel: SupportSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNetworkStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SupportSettingsScreenNonBp(
        uiState = uiState,
        onBackClick = onBackClick,
        onInternetClick = { openNetworkSettings(context) },
        onConnectionStatusClick = viewModel::onConnectionStatusClick,
        onDismissConnectionStatusDialog = viewModel::dismissConnectionStatusDialog,
        onMainServerClick = onMainServerClick,
        onConfigurationClick = onConfigurationClick,
        onMenuFeaturesClick = onMenuFeaturesClick,
        onResetMerchantPassword = viewModel::resetMerchantPassword,
        onTerminalReplacement = viewModel::replaceTerminal,
        onExitClick = onExitClick,
        merchantPasswordResetMessage = uiState.merchantPasswordResetMessage,
        onDismissResetMessage = viewModel::clearMerchantPasswordResetMessage,
        terminalReplacementSuccess = uiState.terminalReplacementSuccess,
        onDismissTerminalReplacementSuccess = viewModel::clearTerminalReplacementSuccess,
        onDismissServiceUnavailableMessage = viewModel::clearServiceUnavailableMessage,
    )
}
