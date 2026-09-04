package com.example.bill.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.api.BillFlowPolicy
import com.danesh.common.strings.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BillInfoUiState(
    val billId: String = "1234000",
    val paymentId: String = "1234000",
    val amount: String = "",
    val billIdError: String? = null,
    val paymentIdError: String? = null,
    val amountError: String? = null,
    val requiresAmountInput: Boolean = false,
)

@HiltViewModel
class BillInfoViewModel @Inject constructor(
    private val appStrings: AppStrings,
    billFlowPolicy: BillFlowPolicy,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BillInfoUiState(requiresAmountInput = billFlowPolicy.requiresAmountInput),
    )
    val uiState: StateFlow<BillInfoUiState> = _uiState.asStateFlow()

    fun onBillIdChange(value: String) {
        _uiState.update { it.copy(billId = value, billIdError = null) }
    }

    fun onPaymentIdChange(value: String) {
        val digits = value.filter { it.isDigit() }
        _uiState.update { it.copy(paymentId = digits, paymentIdError = null) }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value, amountError = null) }
    }

    fun validateAndProceed(onSuccess: (billId: String, paymentId: String, amount: String) -> Unit) {
        val state = _uiState.value
        val trimmedBillId = state.billId.trim()
        val trimmedPaymentId = state.paymentId.trim()
        val trimmedAmount = state.amount.trim()

        var billIdError: String? = null
        var paymentIdError: String? = null
        var amountError: String? = null

        if (trimmedBillId.isEmpty()) {
            billIdError = appStrings.validationEnterBillId()
        }
        if (trimmedPaymentId.isEmpty()) {
            paymentIdError = appStrings.validationEnterPaymentId()
        }
        if (state.requiresAmountInput) {
            if (trimmedAmount.isEmpty()) {
                amountError = appStrings.validationEnterAmount()
            } else {
                val numericAmount = trimmedAmount.replace(",", "").toLongOrNull()
                if (numericAmount == null || numericAmount <= 0L) {
                    amountError = appStrings.validationEnterAmount()
                }
            }
        }

        if (billIdError != null || paymentIdError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    billIdError = billIdError,
                    paymentIdError = paymentIdError,
                    amountError = amountError,
                )
            }
            return
        }

        val amount = if (state.requiresAmountInput) {
            trimmedAmount.replace(",", "")
        } else {
            "0"
        }
        onSuccess(trimmedBillId, trimmedPaymentId, amount)
    }
}
