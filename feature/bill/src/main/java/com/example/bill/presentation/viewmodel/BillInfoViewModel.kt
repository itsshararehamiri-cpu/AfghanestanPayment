package com.example.bill.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.api.BillFlowPolicy
import com.danesh.api.BillPaymentFieldError
import com.danesh.api.BillPaymentValidation
import com.danesh.common.strings.AppStrings
import com.example.bill.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BillInfoUiState(
    val billId: String = "6039628301226",
    val paymentId: String = "189840835",
    val amount: String = "",
    val billIdError: String? = null,
    val paymentIdError: String? = null,
    val amountError: String? = null,
    val requiresAmountInput: Boolean = false,
)

@HiltViewModel
class BillInfoViewModel @Inject constructor(
    private val appStrings: AppStrings,
    private val billFlowPolicy: BillFlowPolicy,
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

        val sadadCheck = billFlowPolicy.validateBillPayment(trimmedBillId, trimmedPaymentId)
        if (sadadCheck != null) {
            applySadadCheck(sadadCheck, onSuccess)
            return
        }

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

    private fun applySadadCheck(
        check: BillPaymentValidation,
        onSuccess: (billId: String, paymentId: String, amount: String) -> Unit,
    ) {
        if (!check.isValid) {
            _uiState.update {
                it.copy(
                    billIdError = check.billIdError?.let(::billIdMessage),
                    paymentIdError = check.paymentIdError?.let(::paymentIdMessage),
                )
            }
            return
        }
        val state = _uiState.value
        onSuccess(state.billId.trim(), state.paymentId.trim(), check.amount.trimStart('0').ifEmpty { "0" })
    }

    private fun billIdMessage(error: BillPaymentFieldError): String = when (error) {
        BillPaymentFieldError.EMPTY -> appStrings.validationEnterBillId()
        BillPaymentFieldError.CHECK_DIGIT -> appStrings.get(R.string.bill_id_check_digit)
        BillPaymentFieldError.TYPE -> appStrings.get(R.string.bill_id_invalid_type)
        BillPaymentFieldError.INVALID,
        BillPaymentFieldError.PAIR,
        -> appStrings.get(R.string.bill_id_invalid)
    }

    private fun paymentIdMessage(error: BillPaymentFieldError): String = when (error) {
        BillPaymentFieldError.EMPTY -> appStrings.validationEnterPaymentId()
        BillPaymentFieldError.CHECK_DIGIT -> appStrings.get(R.string.payment_id_check_digit)
        BillPaymentFieldError.PAIR -> appStrings.get(R.string.payment_id_pair_mismatch)
        BillPaymentFieldError.INVALID,
        BillPaymentFieldError.TYPE,
        -> appStrings.get(R.string.payment_id_invalid)
    }
}
