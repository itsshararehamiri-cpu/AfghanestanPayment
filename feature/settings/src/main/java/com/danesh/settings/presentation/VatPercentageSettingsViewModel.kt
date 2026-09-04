package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.TransactionContextProvider
import com.danesh.api.VatPercentageRules
import com.danesh.api.VatPercentageValidationError
import com.danesh.settings.model.VatPercentageSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class VatPercentageSettingsViewModel @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<VatPercentageSettingsUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        val digits = value.filter(Char::isDigit).take(3)
        if (digits.isNotEmpty()) {
            val number = digits.toIntOrNull() ?: return
            if (number > 100) return
        }
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
        val validationError = VatPercentageRules.validate(_uiState.value.inputValue)
        val errorMessage = when (validationError) {
            VatPercentageValidationError.EMPTY -> emptyError
            VatPercentageValidationError.INVALID -> invalidError
            VatPercentageValidationError.OUT_OF_RANGE -> rangeError
            null -> null
        }
        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        val normalized = VatPercentageRules.normalize(_uiState.value.inputValue)
        contextProvider.saveVatPercentage(normalized)
        _uiState.update {
            it.copy(
                vatPercentage = contextProvider.getVatPercentage(),
                inputValue = contextProvider.getVatPercentage(),
                errorMessage = null,
                savedMessage = savedMessage,
            )
        }
    }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    private fun loadState(): VatPercentageSettingsUiState {
        val current = contextProvider.getVatPercentage()
        return VatPercentageSettingsUiState(
            vatPercentage = current,
            inputValue = current,
        )
    }
}
