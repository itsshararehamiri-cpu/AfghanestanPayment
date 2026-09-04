package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.MicroPaymentIndexRules
import com.danesh.api.MicroPaymentIndexValidationError
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.settings.model.MicroPaymentIndexSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MicroPaymentIndexSettingsViewModel @Inject constructor(
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<MicroPaymentIndexSettingsUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        val digits = value.filter(Char::isDigit).take(12)
        _uiState.update {
            it.copy(inputValue = digits, errorMessage = null, savedMessage = null)
        }
    }

    fun save(
        emptyError: String,
        invalidError: String,
        rangeError: String,
        savedMessage: String,
    ) {
        val validationError = MicroPaymentIndexRules.validate(_uiState.value.inputValue)
        val errorMessage = when (validationError) {
            MicroPaymentIndexValidationError.EMPTY -> emptyError
            MicroPaymentIndexValidationError.INVALID -> invalidError
            MicroPaymentIndexValidationError.OUT_OF_RANGE -> rangeError
            null -> null
        }
        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        val normalized = MicroPaymentIndexRules.normalize(_uiState.value.inputValue)
        merchantDisplayPreferences.setMicroPaymentIndexAmountRials(normalized)
        _uiState.update {
            it.copy(
                amountRials = normalized,
                inputValue = normalized.toString(),
                errorMessage = null,
                savedMessage = savedMessage,
            )
        }
    }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    private fun loadState(): MicroPaymentIndexSettingsUiState {
        val current = MicroPaymentIndexRules.resolve(
            merchantDisplayPreferences.getMicroPaymentIndexAmountRials(),
        )
        return MicroPaymentIndexSettingsUiState(
            amountRials = current,
            inputValue = current.toString(),
        )
    }
}
