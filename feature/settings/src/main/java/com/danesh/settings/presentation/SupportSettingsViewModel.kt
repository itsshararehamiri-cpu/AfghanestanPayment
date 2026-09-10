package com.danesh.settings.presentation

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.MicroPaymentIndexRules
import com.danesh.api.PspGateway
import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TerminalReplacementService
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.SwitchConnectionChecker
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.settings.R
import com.danesh.settings.config.SettingsMenuVisibilityProvider
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.domain.MerchantSupportServiceResolver
import com.danesh.settings.domain.toDeviceConfigurationSummary
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.model.MerchantSupportLaunchRequest
import com.danesh.settings.model.SupportSettingsUiState
import com.danesh.settings.receipt.InitialConfigurationReceiptBitmapFactory
import com.danesh.settings.receipt.KcvReceiptBitmapFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

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
    private val pspGateway: PspGateway,
    private val initialConfigurationPolicy: InitialConfigurationPolicy,
    private val device: Device,
    private val configurationStore: DeviceConfigurationStore,
    private val appVersionProvider: AppVersionProvider,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val receiptPspBrandProvider: ReceiptPspBrandProvider,
    private val localePreferences: LocalePreferences,
    @ApplicationContext private val appContext: Context,

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
            usesSimplifiedSupportSettings = menuVisibility.usesSimplifiedSupportSettings,
            usesTerminalConfigFlow = initialConfigurationPolicy.usesTerminalConfigFlow,
        )
    }

    /**
     * همراه‌پی: ردیف «کلیدگذاری» در صفحه تنظیمات پشتیبان — inject مستقیم کلید بدون
     * جابه‌جایی صفحه؛ نتیجه (و در صورت موفقیت KCV) در دیالوگ نمایش داده می‌شود.
     */
    fun confirmKeyLoading() {
        if (_uiState.value.isKeyLoadingInProgress) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isKeyLoadingInProgress = true, keyLoadingErrorMessage = null, keyLoadingKcvSummary = null)
            }

            val injectResult = runCatching { initialConfigurationPolicy.injectKeys() }
                .getOrElse { Result.failure(it) }

            injectResult.fold(
                onSuccess = {
                    val summary = buildConfigurationSummary()
                    configurationStore.saveConfigurationSummary(summary.toDeviceConfigurationSummary())
                    printConfigurationReceipt(summary)
                    val kcvSummary = printKcvReceiptAndBuildSummary()
                    _uiState.update {
                        it.copy(isKeyLoadingInProgress = false, keyLoadingKcvSummary = kcvSummary)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isKeyLoadingInProgress = false,
                            keyLoadingErrorMessage = error.message.orEmpty().ifBlank {
                                appContext.getString(R.string.settings_initial_configuration_error_key_inject)
                            },
                        )
                    }
                },
            )
        }
    }

    fun dismissKeyLoadingResult() {
        _uiState.update { it.copy(keyLoadingKcvSummary = null) }
    }

    fun dismissKeyLoadingError() {
        _uiState.update { it.copy(keyLoadingErrorMessage = null) }
    }

    /**
     * همراه‌پی: ردیف «پیکربندی پایانه» در صفحه تنظیمات پشتیبان — pspGateway.terminalConfig
     * بدون جابه‌جایی صفحه؛ نتیجه (و در صورت موفقیت اطلاعات پایانه) در دیالوگ نمایش داده می‌شود.
     */
    fun confirmTerminalInfo() {
        if (_uiState.value.isTerminalInfoInProgress) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isTerminalInfoInProgress = true, terminalInfoErrorMessage = null, terminalInfoSummary = null)
            }

            val result = runCatching { pspGateway.terminalConfig(TerminalConfigInput("")) }
                .getOrElse { throwable ->
                    _uiState.update {
                        it.copy(
                            isTerminalInfoInProgress = false,
                            terminalInfoErrorMessage = throwable.message.orEmpty().ifBlank {
                                appContext.getString(R.string.settings_initial_configuration_error_generic)
                            },
                        )
                    }
                    return@launch
                }

            if (result.isSuccess) {
                val summary = buildConfigurationSummary()
                printConfigurationReceipt(summary)
                _uiState.update {
                    it.copy(isTerminalInfoInProgress = false, terminalInfoSummary = summary)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isTerminalInfoInProgress = false,
                        terminalInfoErrorMessage = result.responseMessage.ifBlank {
                            appContext.getString(R.string.settings_initial_configuration_error_failed)
                        },
                    )
                }
            }
        }
    }

    fun dismissTerminalInfoResult() {
        _uiState.update { it.copy(terminalInfoSummary = null) }
    }

    fun dismissTerminalInfoError() {
        _uiState.update { it.copy(terminalInfoErrorMessage = null) }
    }

    private fun buildConfigurationSummary(): InitialConfigurationSummary {
        val serial = runCatching { device.getSerial() }.getOrElse { "" }
        val terminalConfig = contextProvider.getTerminalConfig()
        val appVersion = appVersionProvider.versionName()
        val programDate = ReceiptNowFormatter.format(localePreferences)
        return InitialConfigurationSummary(
            hardwareSerial = serial.ifBlank { "-" },
            terminalId = terminalConfig.terminalId.ifBlank { "-" },
            appVersion = appVersion.ifBlank { "-" },
            programDate = programDate,
            merchantId = terminalConfig.merchantId.ifBlank { "-" },
        )
    }

    private suspend fun printConfigurationReceipt(summary: InitialConfigurationSummary) {
        val bitmap = InitialConfigurationReceiptBitmapFactory.create(
            title = appContext.getString(R.string.settings_initial_configuration_receipt_title),
            hardwareSerialLabel = appContext.getString(R.string.settings_initial_configuration_hardware_serial),
            hardwareSerial = summary.hardwareSerial,
            terminalIdLabel = appContext.getString(R.string.settings_initial_configuration_terminal_id),
            terminalId = summary.terminalId,
            merchantIdLabel = appContext.getString(R.string.settings_initial_configuration_merchant_id),
            merchantId = summary.merchantId,
            appVersionLabel = appContext.getString(R.string.settings_initial_configuration_app_version),
            appVersion = summary.appVersion,
            programDateLabel = appContext.getString(R.string.settings_initial_configuration_program_date),
            programDate = summary.programDate,
            titleTypeface = paperReceiptTypefaceResolver.titleTypeface(appContext),
            lineTypeface = paperReceiptTypefaceResolver.bodyTypeface(appContext),
            lineTextAlign = paperReceiptTypefaceResolver.lineTextAlign(),
            lineX = paperReceiptTypefaceResolver.lineX(
                width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
            ),
        )
        printBitmap(
            bitmap,
            appContext.getString(R.string.settings_initial_configuration_receipt_print_label),
        )
    }

    private suspend fun printKcvReceiptAndBuildSummary(): KeyLoadingKcvSummary {
        val kcv = runCatching { device.getKCv() }.getOrElse { KCV("", "", "", "") }
        val bitmap = KcvReceiptBitmapFactory.create(
            context = appContext,
            title = appContext.getString(R.string.settings_kcv_receipt_title),
            macKeyLabel = appContext.getString(R.string.settings_kcv_mac_key),
            macKeyValue = kcv.mac.ifBlank { "-" },
            dataKeyLabel = appContext.getString(R.string.settings_kcv_data_key),
            dataKeyValue = kcv.data.ifBlank { "-" },
            pinKeyLabel = appContext.getString(R.string.settings_kcv_pin_key),
            pinKeyValue = kcv.pin.ifBlank { "-" },
            fonts = paperReceiptTypefaceResolver.bitmapFonts(
                context = appContext,
                width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
            ),
            pspBrand = receiptPspBrandProvider.current(),
        )
        printBitmap(
            bitmap,
            appContext.getString(R.string.settings_initial_configuration_kcv_receipt_print_label),
        )
        return KeyLoadingKcvSummary(
            master = kcv.master.ifBlank { "-" },
            mac = kcv.mac.ifBlank { "-" },
            pin = kcv.pin.ifBlank { "-" },
            data = kcv.data.ifBlank { "-" },
        )
    }

    private suspend fun printBitmap(bitmap: Bitmap, logLabel: String) {
        suspendCancellableCoroutine { continuation ->
            val job = CoroutineScope(continuation.context).launch {
                device.print(
                    bitmap = bitmap,
                    context = appContext,
                    onSuccess = {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    },
                    onFailed = {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    },
                )
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }
}
