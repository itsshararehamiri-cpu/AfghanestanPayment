package com.danesh.settings.model

data class MicroPaymentIndexSettingsUiState(
    val amountRials: Long = 0L,
    val inputValue: String = "",
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)
