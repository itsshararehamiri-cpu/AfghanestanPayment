package com.danesh.settings.model

import com.danesh.api.KeyCardKcvSummary
import com.danesh.api.KeyCardType

data class SadadKeyCardLoadingUiState(
    val keyIndex: String = "1",
    val cardAPin: String = "",
    val cardBcPin: String = "",
    val selectedCard: KeyCardType = KeyCardType.CARD_C,
    val hasStoredCardAPair: Boolean = false,
    val isCardAStepLoading: Boolean = false,
    val isCardBcStepLoading: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
    val kcvSummary: KeyCardKcvSummary? = null,
) {
    val isLoading: Boolean get() = isCardAStepLoading || isCardBcStepLoading
}
