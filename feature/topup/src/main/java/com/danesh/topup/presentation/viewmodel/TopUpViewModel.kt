package com.danesh.topup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.common.strings.AppStrings
import com.danesh.topup.MobileOperator
import com.danesh.topup.ui.formatMobileInput
import com.danesh.topup.ui.mobileNumberForSubmit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TopUpUiState(
    val mobileNumber: String = "",//7394723567
    val selectedOperator: MobileOperator? =null,
    val selectedAmount: Int? = null,
    val amountText: String = "",
    val mobileError: String? = null,
    val operatorError: String? = null,
    val amountError: String? = null,
)

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    fun onMobileChange(value: String) {
        _uiState.update { it.copy(mobileNumber = formatMobileInput(value), mobileError = null) }
    }

    fun onOperatorSelected(operator: MobileOperator) {
        _uiState.update { it.copy(selectedOperator = operator, operatorError = null) }
    }

    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }
        val amount = digits.toIntOrNull() ?: 0
        _uiState.update {
            it.copy(
                selectedAmount = amount,
                amountText = digits,
                amountError = null,
            )
        }
    }

    fun onPresetAmountSelected(amount: Int) {
        _uiState.update {
            it.copy(
                selectedAmount = amount,
                amountText = amount.toString(),
                amountError = null,
            )
        }
    }

    fun validateAndProceed(onSuccess: (mobile: String, operator: MobileOperator, amount: Int) -> Unit) {
        val state = _uiState.value
        val normalizedMobile = mobileNumberForSubmit(state.mobileNumber)

        var mobileError: String? = null
        var operatorError: String? = null
        var amountError: String? = null

        if (normalizedMobile.isEmpty()) {
            mobileError = appStrings.validationEnterMobile()
        } else if (normalizedMobile.length != 11) {//TODO()10
            mobileError = appStrings.validationMobile10Digits()
        } else if (!normalizedMobile.startsWith("09")) {//TODO()07
            mobileError = appStrings.validationMobilePrefix()
        }

        if (state.selectedOperator == null) {
            operatorError = appStrings.validationSelectOperator()
        }

        if (state.selectedAmount==null || state.selectedAmount <= 0) {
            amountError = appStrings.validationEnterAmount()
        }

        if (mobileError != null || operatorError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    mobileError = mobileError,
                    operatorError = operatorError,
                    amountError = amountError,
                )
            }
            return
        }

        onSuccess(
            normalizedMobile,
            state.selectedOperator!!,
            state.selectedAmount!!,
        )
    }
}
