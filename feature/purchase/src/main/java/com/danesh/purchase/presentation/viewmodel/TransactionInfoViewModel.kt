package com.danesh.purchase.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.danesh.api.DefaultPurchaseAmountDefaults
import com.danesh.api.DefaultPurchaseAmountRules
import com.danesh.api.DefaultPurchaseAmountValidationError
import com.danesh.common.merchant.MerchantDisplayPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TransactionInfoViewModel @Inject constructor(
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<TransactionInfoUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        val digits = value.filter(Char::isDigit)
            .take(DefaultPurchaseAmountDefaults.MAX_DIGITS)
        _uiState.update { it.copy(amount = digits, amountError = null) }
    }

    fun validateAndProceed(context: Context, onSuccess: (amount: String) -> Unit) {
        val state = _uiState.value
        val trimmedAmount = state.amount.trim()

        if (trimmedAmount.isEmpty()) {
            _uiState.update {
                it.copy(amountError = context.getString(com.danesh.purchase.R.string.pla_enter_amount))
            }
            return
        }

        val validationError = DefaultPurchaseAmountRules.validate(trimmedAmount)
        val errorMessage = when (validationError) {
            DefaultPurchaseAmountValidationError.EMPTY ->
                context.getString(com.danesh.purchase.R.string.pla_enter_amount)
            DefaultPurchaseAmountValidationError.INVALID ->
                context.getString(com.danesh.purchase.R.string.invalid_amount_entered)
            DefaultPurchaseAmountValidationError.BELOW_MINIMUM ->
                context.getString(com.danesh.purchase.R.string.amount_below_minimum)
            DefaultPurchaseAmountValidationError.TOO_MANY_DIGITS ->
                context.getString(com.danesh.purchase.R.string.amount_too_many_digits)
            null -> null
        }
        if (errorMessage != null) {
            _uiState.update { it.copy(amountError = errorMessage) }
            return
        }

        onSuccess(DefaultPurchaseAmountRules.formatDigits(DefaultPurchaseAmountRules.normalize(trimmedAmount)))
    }

    private fun loadInitialState(): TransactionInfoUiState {
        val defaultAmount = DefaultPurchaseAmountRules.resolveForPurchase(
            enabled = merchantDisplayPreferences.isDefaultPurchaseAmountEnabled(),
            storedAmountRials = merchantDisplayPreferences.getDefaultPurchaseAmountRials(),
        )
        return TransactionInfoUiState(amount = defaultAmount.orEmpty())
    }
}

data class TransactionInfoUiState(
    val amount: String = "",
    val amountError: String? = null,
)
