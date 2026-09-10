package com.danesh.settings.model

/**
 * صفحه «پیکربندی پایانه» همراه‌پی — کلیدگذاری و دریافت اطلاعات پایانه هر دو
 * در همین صفحه انجام می‌شوند و نتیجه هر بخش بدون جابه‌جایی به صفحه بعد،
 * درون همان بخش نمایش داده می‌شود.
 */
data class HpKeyLoadingSectionState(
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val kcvSummary: KeyLoadingKcvSummary? = null,
)

data class HpTerminalInfoSectionState(
    val isLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val summary: InitialConfigurationSummary? = null,
)

data class HpTerminalProvisioningUiState(
    val keyLoading: HpKeyLoadingSectionState = HpKeyLoadingSectionState(),
    val terminalInfo: HpTerminalInfoSectionState = HpTerminalInfoSectionState(),
)
