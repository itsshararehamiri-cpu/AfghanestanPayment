package com.danesh.settings.model

data class DefaultIdSettingsUiState(
    val enabled: Boolean = false,
    val value: String = "",
    val valueError: String? = null,
    val validationSummary: String? = null,
    val savedSuccessfully: Boolean = false,
)
