package com.danesh.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.strings.AppStrings
import com.danesh.report.data.ReportPasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportPasswordUiState(
    val pinValue: String = "",
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class ReportPasswordViewModel @Inject constructor(
    private val passwordRepository: ReportPasswordRepository,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportPasswordUiState())
    val uiState: StateFlow<ReportPasswordUiState> = _uiState.asStateFlow()

    fun onPinChange(value: String) {
        if (value.length > 4 || value.any { !it.isDigit() }) return
        _uiState.update { it.copy(pinValue = value, errorMessage = null) }
    }

    fun submitPassword(onSuccess: () -> Unit, onMandatoryPasswordChange: () -> Unit = {}) {
        val pin = _uiState.value.pinValue
        if (pin.length != 4) return

        viewModelScope.launch {
            if (passwordRepository.validate(pin)) {
                _uiState.update {
                    it.copy(isAuthenticated = true, errorMessage = null)
                }
                if (passwordRepository.requiresMerchantPasswordChange()) {
                    onMandatoryPasswordChange()
                } else {
                    onSuccess()
                }
            } else {
                _uiState.update {
                    it.copy(
                        pinValue = "",
                        errorMessage = appStrings.wrongPassword(),
                    )
                }
            }
        }
    }
}
