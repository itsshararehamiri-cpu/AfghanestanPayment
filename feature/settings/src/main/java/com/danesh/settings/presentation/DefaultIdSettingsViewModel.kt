package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import com.danesh.common.connection.ConnectionAddressValidator
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.settings.model.DefaultIdSettingsUiState
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.util.SettingsTextInputFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class DefaultIdSettingsViewModel @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<DefaultIdSettingsUiState> = _uiState.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                enabled = enabled,
                valueError = null,
                validationSummary = null,
            )
        }
    }

    fun setValue(value: String) {
        _uiState.update {
            it.copy(
                value = SettingsTextInputFilters.apply(SettingsTextInputFilter.DigitsOnly, value)
                    .take(DefaultDepositIdMaxLength),
                valueError = null,
                validationSummary = null,
            )
        }
    }

    fun save(
        emptyError: String,
        invalidError: String,
        lengthError: String,
    ) {
        val state = _uiState.value
        if (!state.enabled) {
            connectionPreferences.setDefaultDepositIdEnabled(false)
            _uiState.update {
                it.copy(
                    validationSummary = null,
                    valueError = null,
                    savedSuccessfully = true,
                )
            }
            return
        }

        val valueError = when (ConnectionAddressValidator.defaultDepositIdError(state.value)) {
            ConnectionAddressValidator.DefaultDepositIdValidationError.EMPTY -> emptyError
            ConnectionAddressValidator.DefaultDepositIdValidationError.INVALID -> invalidError
            ConnectionAddressValidator.DefaultDepositIdValidationError.LENGTH -> lengthError
            null -> null
        }

        if (valueError != null) {
            _uiState.update {
                it.copy(
                    valueError = valueError,
                    validationSummary = valueError,
                    savedSuccessfully = false,
                )
            }
            return
        }

        connectionPreferences.setDefaultDepositIdEnabled(true)
        connectionPreferences.saveNii(state.value.trim())
        _uiState.update {
            it.copy(
                validationSummary = null,
                valueError = null,
                savedSuccessfully = true,
            )
        }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }

    private fun loadState(): DefaultIdSettingsUiState {
        val enabled = connectionPreferences.isDefaultDepositIdEnabled()
        val stored = connectionPreferences.getNii().trim()
        return DefaultIdSettingsUiState(
            enabled = enabled,
            value = stored.takeIf { enabled && stored.isNotBlank() }.orEmpty(),
        )
    }

    private companion object {
        private const val DefaultDepositIdMaxLength = 30
    }
}
