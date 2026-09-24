package com.danesh.settings.model

import com.danesh.settings.domain.StartupOperation
import com.danesh.settings.domain.StartupStepResult

import com.danesh.common.connection.ConnectionDefaults
import com.danesh.common.network.NetworkConnectionType

data class SupportSettingsUiState(
    val defaultMerchantPassword: String = "",
    val isConnected: Boolean = false,
    val ipAddress: String = ConnectionDefaults.IP,
    val port: String = ConnectionDefaults.PORT.toString(),
    val terminalInfo: String = "-",
    val configuration: String = "-",
    val merchantPasswordResetMessage: String? = null,
    val terminalReplacementSuccess: Boolean = false,
    val vatPercentage: String = "",
    val showConnectionStatusDialog: Boolean = false,
    val isCheckingConnectionStatus: Boolean = false,
    val connectionType: NetworkConnectionType = NetworkConnectionType.NONE,
    val isServerConnected: Boolean = false,
    val tmsIpAddress: String = ConnectionDefaults.TMS_IP,
    val tmsPort: String = ConnectionDefaults.TMS_PORT.toString(),
    val tmUpdateServerNotConfiguredMessage: String? = null,
    val serviceUnavailableMessage: String? = null,
    val showSupportServices: Boolean = true,
    val showVatPercentage: Boolean = true,
    val showTmsSection: Boolean = true,
    val showShowFee: Boolean = false,
    val showFeeEnabled: Boolean = false,
    val showMicroPaymentIndex: Boolean = false,
    val microPaymentIndexAmount: String = "",
    val usesSimplifiedSupportSettings: Boolean = false,
    /** همراه‌پی: ردیف «کلیدگذاری» مستقیم و ردیف «پیکربندی پایانه» بدون جابه‌جایی صفحه. */
    val usesTerminalConfigFlow: Boolean = false,
    val isKeyLoadingInProgress: Boolean = false,
    val keyLoadingKcvSummary: KeyLoadingKcvSummary? = null,
    val keyLoadingErrorMessage: String? = null,
    val isTerminalInfoInProgress: Boolean = false,
    val terminalInfoSummary: InitialConfigurationSummary? = null,
    val terminalInfoErrorMessage: String? = null,
    /** سداد: به‌جای «پیکربندی پایانه» سه ردیف «تزریق کلید»، «پیکربندی» (INIT) و «شروع به کار» (LOGON). */
    val usesKeyCardSetup: Boolean = false,
    val startupInProgress: StartupOperation? = null,
    val startupResult: StartupStepResult? = null,
)
