package com.danesh.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.KeyCardLoadingService
import com.danesh.api.KeyCardPinRejectedException
import com.danesh.api.KeyCardType
import com.danesh.settings.R
import com.danesh.settings.model.SadadKeyCardLoadingUiState
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.util.SettingsTextInputFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_PIN_LENGTH = 8
private const val MAX_KEY_INDEX_LENGTH = 3

@HiltViewModel
class SadadKeyCardLoadingViewModel @Inject constructor(
    private val service: KeyCardLoadingService,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SadadKeyCardLoadingUiState())
    val uiState: StateFlow<SadadKeyCardLoadingUiState> = _uiState.asStateFlow()

    init {
        refreshStoredCardAState()
    }

    private fun refreshStoredCardAState() {
        viewModelScope.launch {
            val keyIndex = _uiState.value.keyIndex.toIntOrNull() ?: return@launch
            val hasStored = runCatching { service.hasStoredKeyPair(keyIndex) }.getOrDefault(false)
            _uiState.update { it.copy(hasStoredCardAPair = hasStored) }
        }
    }

    fun updateKeyIndex(value: String) {
        if (_uiState.value.isLoading) return
        val filtered = SettingsTextInputFilters
            .apply(SettingsTextInputFilter.PositiveInteger, value)
            .take(MAX_KEY_INDEX_LENGTH)
        _uiState.update { it.copy(keyIndex = filtered, resultMessage = null, isSuccess = false) }
        refreshStoredCardAState()
    }

    fun updateCardAPin(value: String) {
        if (_uiState.value.isCardAStepLoading) return
        val filtered = SettingsTextInputFilters
            .apply(SettingsTextInputFilter.DigitsOnly, value)
            .take(MAX_PIN_LENGTH)
        _uiState.update { it.copy(cardAPin = filtered, resultMessage = null, isSuccess = false) }
    }

    fun updateCardBcPin(value: String) {
        if (_uiState.value.isCardBcStepLoading) return
        val filtered = SettingsTextInputFilters
            .apply(SettingsTextInputFilter.DigitsOnly, value)
            .take(MAX_PIN_LENGTH)
        _uiState.update { it.copy(cardBcPin = filtered, resultMessage = null, isSuccess = false) }
    }

    fun selectCard(card: KeyCardType) {
        if (_uiState.value.isCardBcStepLoading) return
        if (card == KeyCardType.CARD_A) return
        _uiState.update { it.copy(selectedCard = card) }
    }

    fun readCardA() {
        if (_uiState.value.isLoading) return
        val keyIndex = _uiState.value.keyIndex.toIntOrNull()
        if (keyIndex == null) {
            _uiState.update {
                it.copy(resultMessage = appContext.getString(R.string.settings_key_card_error_key_index_required))
            }
            return
        }
        val pin = _uiState.value.cardAPin
        if (pin.length < 4) {
            _uiState.update {
                it.copy(resultMessage = appContext.getString(R.string.settings_key_card_error_pin_required))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isCardAStepLoading = true, resultMessage = null, isSuccess = false)
            }
            val result = service.loadKeyPairFromCardA(pin, keyIndex)
            _uiState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isCardAStepLoading = false,
                            isSuccess = true,
                            hasStoredCardAPair = true,
                            cardAPin = "",
                            resultMessage = appContext.getString(R.string.settings_key_card_success_card_a),
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isCardAStepLoading = false,
                            cardAPin = "",
                            resultMessage = describeError(error),
                        )
                    },
                )
            }
        }
    }

    fun readCardBOrC() {
        if (_uiState.value.isLoading) return
        val keyIndex = _uiState.value.keyIndex.toIntOrNull()
        if (keyIndex == null) {
            _uiState.update {
                it.copy(resultMessage = appContext.getString(R.string.settings_key_card_error_key_index_required))
            }
            return
        }
        val pin = _uiState.value.cardBcPin
        if (pin.length < 4) {
            _uiState.update {
                it.copy(resultMessage = appContext.getString(R.string.settings_key_card_error_pin_required))
            }
            return
        }
        val card = _uiState.value.selectedCard

        viewModelScope.launch {
            _uiState.update {
                it.copy(isCardBcStepLoading = true, resultMessage = null, isSuccess = false, kcvSummary = null)
            }
            val result = service.loadAndInjectMasterKeys(card, pin, keyIndex)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { summary ->
                        state.copy(
                            isCardBcStepLoading = false,
                            isSuccess = true,
                            cardBcPin = "",
                            kcvSummary = summary,
                            resultMessage = appContext.getString(R.string.settings_key_card_success_card_bc),
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isCardBcStepLoading = false,
                            cardBcPin = "",
                            resultMessage = describeError(error),
                        )
                    },
                )
            }
        }
    }

    private fun describeError(error: Throwable): String = when (error) {
        is KeyCardPinRejectedException -> appContext.getString(
            R.string.settings_key_card_error_pin_rejected,
            error.remainingTries,
        )
        else -> appContext.getString(
            R.string.settings_key_card_error_generic,
            error.message.orEmpty(),
        )
    }
}
