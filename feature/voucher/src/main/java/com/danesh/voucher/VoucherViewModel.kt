package com.danesh.voucher

import androidx.lifecycle.ViewModel
import com.danesh.common.strings.AppStrings
import com.danesh.voucher.model.MobileOperator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VoucherUiState(
    val selectedOperator: MobileOperator? = null,
    val selectedAmount: Int?=null ,
    val amountText: String="" ,
    val operatorError: String? = null,
    val amountError: String? = null,
)
@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    fun onOperatorSelected(operator: MobileOperator) {
        _uiState.update { it.copy(selectedOperator = operator, operatorError = null) }
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

    fun validateAndProceed(onSuccess: ( operator: MobileOperator, amount: Int) -> Unit) {
        val state = _uiState.value
        var operatorError: String? = null
        var amountError: String? = null
        if (state.selectedOperator == null) {
            operatorError = appStrings.validationSelectOperator()
        }
        if (state.selectedAmount==null || state.selectedAmount <= 0) {
            amountError = appStrings.validationEnterAmount()
        }
        if ( operatorError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    operatorError = operatorError,
                    amountError = amountError,
                )
            }
            return
        }
        onSuccess(
            state.selectedOperator!!,
            state.selectedAmount!!,
        )
    }
}
