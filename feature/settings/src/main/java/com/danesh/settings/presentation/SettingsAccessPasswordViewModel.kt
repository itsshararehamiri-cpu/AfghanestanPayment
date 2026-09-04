package com.danesh.settings.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.strings.AppStrings
import com.danesh.settings.domain.ValidateSettingsPasswordUseCase
import com.danesh.settings.model.AppRole
import com.danesh.settings.navigation.SettingsNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsAccessPasswordUiState(
    val pinValue: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class SettingsAccessPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val validatePassword: ValidateSettingsPasswordUseCase,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val role: AppRole = savedStateHandle.get<String>(SettingsNavArgs.ROLE)
        ?.let { runCatching { AppRole.valueOf(it) }.getOrNull() }
        ?: AppRole.Merchant

    private val _uiState = MutableStateFlow(SettingsAccessPasswordUiState())
    val uiState: StateFlow<SettingsAccessPasswordUiState> = _uiState.asStateFlow()

    fun onPinChange(value: String) {
        if (value.length > 4 || value.any { !it.isDigit() }) return
        _uiState.update { it.copy(pinValue = value, errorMessage = null) }
    }

    fun submitPassword(
        onSuccess: () -> Unit,
        onMandatoryPasswordChange: () -> Unit = {},
    ) {
        val pin = _uiState.value.pinValue
        if (pin.length != 4) return

        viewModelScope.launch {
            if (validatePassword(role, pin)) {
                _uiState.update { it.copy(errorMessage = null) }
                if (role == AppRole.Merchant && validatePassword.requiresMerchantPasswordChange()) {
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
