package com.danesh.cashdeposit.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TransactionInfoViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionInfoUiState())
    val uiState: StateFlow<TransactionInfoUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value, amountError = null) }
    }

    fun validateAndProceed(context: Context, onSuccess: (amount: String) -> Unit) {
        val state = _uiState.value
        val trimmedAmount = state.amount.trim()

        if (trimmedAmount.isEmpty()) {
            _uiState.update {
                it.copy(amountError = context.getString(com.danesh.cashdeposit.R.string.pla_enter_amount))
            }
            return
        }

        val numericAmount = trimmedAmount.replace(",", "").toDoubleOrNull()
        if (numericAmount == null || numericAmount <= 0) {
            _uiState.update {
                it.copy(amountError = context.getString(com.danesh.cashdeposit.R.string.invalid_amount_entered))
            }
            return
        }

        onSuccess(trimmedAmount)
    }
}

data class TransactionInfoUiState(
    val amount: String = "",
    val amountError: String? = null,
)
