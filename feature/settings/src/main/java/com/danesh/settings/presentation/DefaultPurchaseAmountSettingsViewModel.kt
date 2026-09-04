package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.api.DefaultPurchaseAmountRules
import com.danesh.api.DefaultPurchaseAmountValidationError
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.settings.model.DefaultPurchaseAmountSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class DefaultPurchaseAmountSettingsViewModel @Inject constructor(
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<DefaultPurchaseAmountSettingsUiState> = _uiState.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                enabled = enabled,
                amountError = null,
            )
        }
    }

    fun onAmountChange(value: String) {
        val digits = value.filter(Char::isDigit)
        _uiState.update {
            it.copy(amountDigits = digits, amountError = null)
        }
    }

    fun save(
        emptyError: String,
        invalidError: String,
        belowMinimumError: String,
        tooManyDigitsError: String,
    ) {
        val state = _uiState.value
        if (!state.enabled) {
            merchantDisplayPreferences.setDefaultPurchaseAmountEnabled(false)
            merchantDisplayPreferences.clearDefaultPurchaseAmountRials()
            _uiState.update {
                it.copy(
                    amountError = null,
                    amountDigits = "",
                    savedSuccessfully = true,
                )
            }
            return
        }

        val validationError = DefaultPurchaseAmountRules.validate(state.amountDigits)
        val errorMessage = when (validationError) {
            DefaultPurchaseAmountValidationError.EMPTY -> emptyError
            DefaultPurchaseAmountValidationError.INVALID -> invalidError
            DefaultPurchaseAmountValidationError.BELOW_MINIMUM -> belowMinimumError
            DefaultPurchaseAmountValidationError.TOO_MANY_DIGITS -> tooManyDigitsError
            null -> null
        }
        if (errorMessage != null) {
            _uiState.update {
                it.copy(amountError = errorMessage, savedSuccessfully = false)
            }
            return
        }

        val amount = DefaultPurchaseAmountRules.normalize(state.amountDigits)
        merchantDisplayPreferences.setDefaultPurchaseAmountEnabled(true)
        merchantDisplayPreferences.setDefaultPurchaseAmountRials(amount)
        _uiState.update {
            it.copy(
                amountDigits = DefaultPurchaseAmountRules.formatDigits(amount),
                amountError = null,
                savedSuccessfully = true,
            )
        }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }

    private fun loadState(): DefaultPurchaseAmountSettingsUiState {
        val enabled = merchantDisplayPreferences.isDefaultPurchaseAmountEnabled()
        val stored = merchantDisplayPreferences.getDefaultPurchaseAmountRials()
        return DefaultPurchaseAmountSettingsUiState(
            enabled = enabled,
            amountDigits = if (enabled && stored != null && stored > 0L) {
                DefaultPurchaseAmountRules.formatDigits(stored)
            } else {
                ""
            },
        )
    }
}
