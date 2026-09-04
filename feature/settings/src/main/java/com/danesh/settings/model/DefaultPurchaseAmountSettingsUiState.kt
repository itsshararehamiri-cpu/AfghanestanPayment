package com.danesh.settings.model

data class DefaultPurchaseAmountSettingsUiState(
    val enabled: Boolean = false,
    val amountDigits: String = "",
    val amountError: String? = null,
    val savedSuccessfully: Boolean = false,
)
