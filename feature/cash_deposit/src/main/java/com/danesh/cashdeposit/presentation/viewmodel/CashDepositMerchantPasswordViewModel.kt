package com.danesh.cashdeposit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.common.strings.AppStrings
import com.danesh.settings.data.SettingsPasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CashDepositMerchantPasswordUiState(
    val pinValue: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class CashDepositMerchantPasswordViewModel @Inject constructor(
    private val passwordRepository: SettingsPasswordRepository,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashDepositMerchantPasswordUiState())
    val uiState: StateFlow<CashDepositMerchantPasswordUiState> = _uiState.asStateFlow()

    fun onPinChange(value: String) {
        if (value.length > 4 || value.any { !it.isDigit() }) return
        _uiState.update { it.copy(pinValue = value, errorMessage = null) }
    }

    fun submitPassword(onSuccess: () -> Unit) {
        val pin = _uiState.value.pinValue
        if (pin.length != 4) return

        if (passwordRepository.validateMerchantPassword(pin)) {
            _uiState.update { it.copy(pinValue = "", errorMessage = null) }
            onSuccess()
        } else {
            _uiState.update {
                it.copy(pinValue = "", errorMessage = appStrings.wrongPassword())
            }
        }
    }
}
