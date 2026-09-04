package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.MicroPaymentIndexRules
import com.danesh.api.PspGateway
import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import com.danesh.api.TerminalReplacementService
import com.danesh.api.TransactionContextProvider
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.SwitchConnectionChecker
import com.danesh.settings.R
import com.danesh.settings.config.SettingsMenuVisibilityProvider
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.domain.MerchantSupportServiceResolver
import com.danesh.settings.model.MerchantSupportLaunchRequest
import com.danesh.settings.model.SupportSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportSettingsViewModel @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    private val switchConnectionChecker: SwitchConnectionChecker,
    private val passwordRepository: SettingsPasswordRepository,
    private val contextProvider: TransactionContextProvider,
    private val terminalReplacementService: TerminalReplacementService,
    private val supportCatalog: SupportCatalog,
    private val settingsMenuVisibilityProvider: SettingsMenuVisibilityProvider,
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
    private val merchantSupportServiceResolver: MerchantSupportServiceResolver,
    private val pspGateway: PspGateway

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<SupportSettingsUiState> = _uiState.asStateFlow()

    private val _launchSupportService = MutableSharedFlow<MerchantSupportLaunchRequest>()
    val launchSupportService: SharedFlow<MerchantSupportLaunchRequest> =
        _launchSupportService.asSharedFlow()

    init {
        viewModelScope.launch {
            networkConnectivityMonitor.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
    }

    fun refreshNetworkStatus() {
        networkConnectivityMonitor.refresh()
    }

    fun onConnectionStatusClick() {
        networkConnectivityMonitor.refresh()
        _uiState.update {
            it.copy(
                showConnectionStatusDialog = true,
                isCheckingConnectionStatus = true,
                connectionType = networkConnectivityMonitor.connectionType.value,
                isServerConnected = false,
            )
        }
        viewModelScope.launch {
            val isServerConnected = switchConnectionChecker.isServerReachable()
            _uiState.update {
                it.copy(
                    isCheckingConnectionStatus = false,
                    connectionType = networkConnectivityMonitor.connectionType.value,
                    isServerConnected = isServerConnected,
                )
            }
        }
    }

    fun dismissConnectionStatusDialog() {
        _uiState.update { it.copy(showConnectionStatusDialog = false) }
    }

    fun saveIp(ip: String) {
        connectionPreferences.saveIp(ip)
        refreshConnectionSettings()
    }
    fun saveTMSIp(ip: String) {
        connectionPreferences.saveTMSIp(ip)
        refreshTMSSettings()
    }

    fun savePort(port: String) {
        connectionPreferences.savePort(port.toInt())
        refreshConnectionSettings()
    }
    fun saveTMSPort(port: String) {
        connectionPreferences.saveTMSPort(port.toInt())
        refreshTMSSettings()
    }

    fun resetMerchantPassword() {
        passwordRepository.resetMerchantPassword()
        _uiState.update {
            it.copy(
                merchantPasswordResetMessage = SettingsPasswordRepository.DEFAULT_MERCHANT_PASSWORD,
            )
        }
    }

    fun clearMerchantPasswordResetMessage() {
        _uiState.update { it.copy(merchantPasswordResetMessage = null) }
    }

    fun replaceTerminal() {
        viewModelScope.launch {
            terminalReplacementService.replaceTerminal()
            _uiState.value = loadState().copy(terminalReplacementSuccess = true)
        }
    }


    fun clearTerminalReplacementSuccess() {
        _uiState.update { it.copy(terminalReplacementSuccess = false) }
    }

    fun refreshVatPercentage() {
        _uiState.update { it.copy(vatPercentage = contextProvider.getVatPercentage()) }
    }

    fun refreshMicroPaymentIndexAmount() {
        val amount = MicroPaymentIndexRules.resolve(
            merchantDisplayPreferences.getMicroPaymentIndexAmountRials(),
        )
        _uiState.update {
            it.copy(microPaymentIndexAmount = MicroPaymentIndexRules.formatDisplay(amount))
        }
    }

    fun onTmUpdateClick(
        unavailableMessage: String,
        serverNotConfiguredMessage: String,
    ) {
        if (!connectionPreferences.isTmsServerConfigured()) {
            _uiState.update {
                it.copy(tmUpdateServerNotConfiguredMessage = serverNotConfiguredMessage)
            }
            return
        }
        launchSupportService(
            service = merchantSupportServiceResolver.findTmUpdate(supportCatalog),
            unavailableMessage = unavailableMessage,
        )
    }

    fun clearTmUpdateServerNotConfiguredMessage() {
        _uiState.update { it.copy(tmUpdateServerNotConfiguredMessage = null) }
    }

    fun clearServiceUnavailableMessage() {
        _uiState.update { it.copy(serviceUnavailableMessage = null) }
    }

    fun setShowFeeEnabled(enabled: Boolean) {
        merchantDisplayPreferences.setShowFeeEnabled(enabled)
        _uiState.update { it.copy(showFeeEnabled = enabled) }
    }

    private fun launchSupportService(
        service: SupportMenuItem?,
        unavailableMessage: String,
    ) {
        if (service == null) {
            _uiState.update { it.copy(serviceUnavailableMessage = unavailableMessage) }
            return
        }
        viewModelScope.launch {
            _launchSupportService.emit(
                MerchantSupportLaunchRequest(
                    amount = service.amount,
                    serviceId = service.serviceId,
                    title = service.title,
                ),
            )
        }
    }

    private fun refreshConnectionSettings() {
        _uiState.update {
            it.copy(
                ipAddress = connectionPreferences.getIp(),
                port = connectionPreferences.getPort().toString(),
            )
        }
    }
    private fun refreshTMSSettings() {
        _uiState.update {
            it.copy(
                tmsIpAddress = connectionPreferences.getTMSIp(),
                tmsPort = connectionPreferences.getTMSPort().toString(),
            )
        }
    }

    private fun loadState(): SupportSettingsUiState {
        val menuVisibility = settingsMenuVisibilityProvider.visibility()
        val microPaymentAmount = MicroPaymentIndexRules.resolve(
            merchantDisplayPreferences.getMicroPaymentIndexAmountRials(),
        )
        return SupportSettingsUiState(
            isConnected = networkConnectivityMonitor.isConnected.value,
            ipAddress = connectionPreferences.getIp(),
            port = connectionPreferences.getPort().toString(),
            vatPercentage = contextProvider.getVatPercentage(),
            tmsIpAddress = connectionPreferences.getTMSIp(),
            tmsPort = connectionPreferences.getTMSPort().toString(),
            showSupportServices = menuVisibility.showSupportServices,
            showVatPercentage = menuVisibility.showVatPercentage,
            showTmsSection = menuVisibility.showTmsSection,
            showShowFee = menuVisibility.showMerchantShowFee,
            showFeeEnabled = merchantDisplayPreferences.isShowFeeEnabled(),
            showMicroPaymentIndex = menuVisibility.showSupportMicroPaymentIndex,
            microPaymentIndexAmount = MicroPaymentIndexRules.formatDisplay(microPaymentAmount),
        )
    }
}
