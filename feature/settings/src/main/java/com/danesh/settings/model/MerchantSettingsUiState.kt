package com.danesh.settings.model

import com.danesh.common.network.NetworkConnectionType

data class MerchantSettingsUiState(
    val isChangeAccountEnabled: Boolean = true,
    val isConnected: Boolean = false,
    val ipAddress: String = "",
    val port: String = "",
    val defaultNii: String = "",
    val supportPlatformLabel: String = "",
    val showFeeEnabled: Boolean = false,
    val microPaymentIndexEnabled: Boolean = false,
    val defaultPurchaseAmountEnabled: Boolean = false,
    val defaultPurchaseAmountDisplay: String = "-",
    val showDefaultPurchaseAmount: Boolean = false,
    val serviceUnavailableMessage: String? = null,
    val isSettlingWithCenter: Boolean = false,
    val settlementNoTransactionsMessage: String? = null,
    val settlementSuccessMessage: String? = null,
    val showConnectionStatusDialog: Boolean = false,
    val isCheckingConnectionStatus: Boolean = false,
    val connectionType: NetworkConnectionType = NetworkConnectionType.NONE,
    val isServerConnected: Boolean = false,
    val terminalReplacementSuccess: Boolean = false,
    val merchantPasswordResetMessage: String? = null,
    val showShowFee: Boolean = true,
    val showMicroPaymentIndex: Boolean = true,
)

data class MerchantSupportLaunchRequest(
    val amount: String,
    val serviceId: String,
    val title: String,
    val track2: String = "",
    val pan: String = "",
)
