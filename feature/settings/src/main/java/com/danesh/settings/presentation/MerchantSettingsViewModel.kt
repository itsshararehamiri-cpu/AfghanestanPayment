package com.danesh.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.DefaultPurchaseAmountRules
import com.danesh.api.SupportCatalog
import com.danesh.api.TerminalReplacementService
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.common.locale.LocaleManager
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.SwitchConnectionChecker
import com.danesh.common.psp.PspPlatformLabelProvider
import com.danesh.common.receipt.MerchantReceiptPrintMode
import com.danesh.common.receipt.MerchantReceiptPrintPreferences
import com.danesh.common.receipt.SafQueueSettlementService
import com.danesh.menu.model.MenuItemType
import com.danesh.settings.config.SettingsMenuVisibilityProvider
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.domain.MerchantSupportServiceResolver
import com.danesh.settings.locale.SettingsLanguageOptions
import com.danesh.settings.model.AppLanguage
import com.danesh.settings.model.MerchantSettingsUiState
import com.danesh.settings.model.MerchantSupportLaunchRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MerchantSettingsViewModel @Inject constructor(
    private val localePreferences: LocalePreferences,
    private val languageOptions: SettingsLanguageOptions,
    private val merchantReceiptPrintPreferences: MerchantReceiptPrintPreferences,
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
    private val menuFeaturePreferences: MenuFeaturePreferences,
    private val supportCatalog: SupportCatalog,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    private val switchConnectionChecker: SwitchConnectionChecker,
    private val connectionPreferences: ConnectionPreferences,
    private val pspPlatformLabelProvider: PspPlatformLabelProvider,
    private val terminalReplacementService: TerminalReplacementService,
    private val passwordRepository: SettingsPasswordRepository,
    private val safQueueSettlementService: SafQueueSettlementService,
    private val settingsMenuVisibilityProvider: SettingsMenuVisibilityProvider,
    private val merchantSupportServiceResolver: MerchantSupportServiceResolver,
) : ViewModel() {

    val availableLanguages: List<AppLanguage> = languageOptions.availableSettingsLanguages()

    private val _selectedLanguage = MutableStateFlow(currentSettingsLanguage())
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _merchantReceiptPrintMode =
        MutableStateFlow(merchantReceiptPrintPreferences.getMode())
    val merchantReceiptPrintMode: StateFlow<MerchantReceiptPrintMode> =
        _merchantReceiptPrintMode.asStateFlow()

    private val _uiState = MutableStateFlow(loadUiState())
    val uiState: StateFlow<MerchantSettingsUiState> = _uiState.asStateFlow()

    private val _launchSupportService = MutableSharedFlow<MerchantSupportLaunchRequest>()
    val launchSupportService: SharedFlow<MerchantSupportLaunchRequest> =
        _launchSupportService.asSharedFlow()

    init {
        val saved = localePreferences.getLanguage()
        val coerced = languageOptions.coerceCoreLanguage(saved)
        if (coerced != saved) {
            localePreferences.setLanguage(coerced)
            LocaleManager.apply(coerced)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(coerced.localeTag),
            )
        }

        viewModelScope.launch {
            menuFeaturePreferences.disabledFeatures.collect { disabled ->
                _uiState.update {
                    it.copy(
                        isChangeAccountEnabled = MenuItemType.CHANGE_ACCOUNT.name !in disabled,
                        showDefaultPurchaseAmount = MenuItemType.PURCHASE.name !in disabled,
                    )
                }
            }
        }

        viewModelScope.launch {
            networkConnectivityMonitor.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        val coreLanguage = languageOptions.toCoreLanguage(language)
        if (localePreferences.getLanguage() == coreLanguage) {
            _selectedLanguage.value = language
            return
        }
        localePreferences.setLanguage(coreLanguage)
        LocaleManager.apply(coreLanguage)
        _selectedLanguage.value = language
    }

    fun setMerchantReceiptPrintMode(mode: MerchantReceiptPrintMode) {
        merchantReceiptPrintPreferences.setMode(mode)
        _merchantReceiptPrintMode.value = mode
    }

    fun saveDefaultNii(nii: String) {
        connectionPreferences.saveNii(nii)
        _uiState.update { it.copy(defaultNii = connectionPreferences.getNii()) }
    }

    fun setShowFeeEnabled(enabled: Boolean) {
        merchantDisplayPreferences.setShowFeeEnabled(enabled)
        _uiState.update { it.copy(showFeeEnabled = enabled) }
    }

    fun setMicroPaymentIndexEnabled(enabled: Boolean) {
        merchantDisplayPreferences.setMicroPaymentIndexEnabled(enabled)
        _uiState.update { it.copy(microPaymentIndexEnabled = enabled) }
    }

    fun refreshMerchantDisplayPrefs() {
        val defaultAmountEnabled = merchantDisplayPreferences.isDefaultPurchaseAmountEnabled()
        _uiState.update {
            it.copy(
                showFeeEnabled = merchantDisplayPreferences.isShowFeeEnabled(),
                microPaymentIndexEnabled = merchantDisplayPreferences.isMicroPaymentIndexEnabled(),
                defaultPurchaseAmountEnabled = defaultAmountEnabled,
                defaultPurchaseAmountDisplay = DefaultPurchaseAmountRules.displayForMerchantSettings(
                    enabled = defaultAmountEnabled,
                    storedAmountRials = merchantDisplayPreferences.getDefaultPurchaseAmountRials(),
                ),
                showDefaultPurchaseAmount = menuFeaturePreferences.isFeatureEnabled(
                    MenuItemType.PURCHASE.name,
                ),
            )
        }
    }

    fun onSettlementClick(
        noTransactionsMessage: String,
        successMessage: String,
    ) {
        if (_uiState.value.isSettlingWithCenter) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSettlingWithCenter = true,
                    settlementNoTransactionsMessage = null,
                    settlementSuccessMessage = null,
                )
            }
            val hasPending = safQueueSettlementService.hasPendingTransactions()
            if (!hasPending) {
                _uiState.update {
                    it.copy(
                        isSettlingWithCenter = false,
                        settlementNoTransactionsMessage = noTransactionsMessage,
                    )
                }
                return@launch
            }
            // ارسال Advice/Reverse (و نمایش رسید معلق در صورت چاپ‌نشدن) به مرکز
            runCatching { safQueueSettlementService.settleWithCenter() }
            _uiState.update {
                it.copy(
                    isSettlingWithCenter = false,
                    settlementSuccessMessage = successMessage,
                )
            }
        }
    }

    fun onChangeAccountClick(unavailableMessage: String) {
        launchSupportService(
            service = merchantSupportServiceResolver.findChangeAccount(supportCatalog),
            unavailableMessage = unavailableMessage,
        )
    }

    fun onConnectionStatusClick() {
        // نوع اتصال شبکه + بررسی Telnet به سوییچ
        networkConnectivityMonitor.refresh()
        _uiState.update {
            it.copy(
                showConnectionStatusDialog = true,
                isCheckingConnectionStatus = true,
                isServerConnected = false,
                connectionType = networkConnectivityMonitor.connectionType.value,
                ipAddress = connectionPreferences.getIp(),
                port = connectionPreferences.getPort().toString(),
            )
        }
        viewModelScope.launch {
            val isServerConnected = switchConnectionChecker.isServerReachable()
            networkConnectivityMonitor.refresh()
            _uiState.update {
                it.copy(
                    isCheckingConnectionStatus = false,
                    isServerConnected = isServerConnected,
                    connectionType = networkConnectivityMonitor.connectionType.value,
                    ipAddress = connectionPreferences.getIp(),
                    port = connectionPreferences.getPort().toString(),
                )
            }
        }
    }

    fun dismissConnectionStatusDialog() {
        _uiState.update { it.copy(showConnectionStatusDialog = false) }
    }

    fun clearServiceUnavailableMessage() {
        _uiState.update { it.copy(serviceUnavailableMessage = null) }
    }

    fun clearSettlementNoTransactionsMessage() {
        _uiState.update { it.copy(settlementNoTransactionsMessage = null) }
    }

    fun clearSettlementSuccessMessage() {
        _uiState.update { it.copy(settlementSuccessMessage = null) }
    }

    fun replaceTerminal() {
        viewModelScope.launch {
            terminalReplacementService.replaceTerminal()
            _selectedLanguage.value = currentSettingsLanguage()
            _merchantReceiptPrintMode.value = merchantReceiptPrintPreferences.getMode()
            _uiState.value = loadUiState().copy(terminalReplacementSuccess = true)
        }
    }

    fun clearTerminalReplacementSuccess() {
        _uiState.update { it.copy(terminalReplacementSuccess = false) }
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

    fun requiresPasswordChange(): Boolean = passwordRepository.requiresMerchantPasswordChange()

    private fun launchSupportService(
        service: com.danesh.api.SupportMenuItem?,
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

    private fun loadUiState(): MerchantSettingsUiState {
        val menuVisibility = settingsMenuVisibilityProvider.visibility()
        val defaultAmountEnabled = merchantDisplayPreferences.isDefaultPurchaseAmountEnabled()
        return MerchantSettingsUiState(
            isChangeAccountEnabled = menuFeaturePreferences.isFeatureEnabled(
                MenuItemType.CHANGE_ACCOUNT.name,
            ),
            isConnected = networkConnectivityMonitor.isConnected.value,
            ipAddress = connectionPreferences.getIp(),
            port = connectionPreferences.getPort().toString(),
            defaultNii = connectionPreferences.getNii(),
            supportPlatformLabel = pspPlatformLabelProvider.displayName(),
            showFeeEnabled = merchantDisplayPreferences.isShowFeeEnabled(),
            microPaymentIndexEnabled = merchantDisplayPreferences.isMicroPaymentIndexEnabled(),
            defaultPurchaseAmountEnabled = defaultAmountEnabled,
            defaultPurchaseAmountDisplay = DefaultPurchaseAmountRules.displayForMerchantSettings(
                enabled = defaultAmountEnabled,
                storedAmountRials = merchantDisplayPreferences.getDefaultPurchaseAmountRials(),
            ),
            showDefaultPurchaseAmount = menuFeaturePreferences.isFeatureEnabled(
                MenuItemType.PURCHASE.name,
            ),
            showShowFee = menuVisibility.showMerchantShowFee,
            showMicroPaymentIndex = menuVisibility.showMerchantMicroPaymentIndex,
        )
    }

    private fun currentSettingsLanguage(): AppLanguage {
        val coerced = languageOptions.coerceCoreLanguage(localePreferences.getLanguage())
        return languageOptions.toSettingsLanguage(coerced)
    }
}
