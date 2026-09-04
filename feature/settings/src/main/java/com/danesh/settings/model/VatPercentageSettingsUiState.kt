package com.danesh.settings.model

data class VatPercentageSettingsUiState(
    val vatPercentage: String = "",
    val inputValue: String = "",
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)
