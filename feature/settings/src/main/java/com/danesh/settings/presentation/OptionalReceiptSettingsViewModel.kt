package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.OptionalReceiptLimits
import com.danesh.api.OptionalReceiptRules
import com.danesh.api.OptionalReceiptValidationError
import com.danesh.api.PspGateway
import com.danesh.common.receipt.OptionalReceiptStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OptionalReceiptSettingsUiState(
    val lowerInput: String = "",
    val upperInput: String = "",
    val lowerError: String? = null,
    val upperError: String? = null,
    val inProgress: Boolean = false,
    /** پیام نتیجه (موفق/ناموفق) برای دیالوگ. */
    val resultMessage: String? = null,
    val resultSuccess: Boolean = false,
)

/** متن‌های قابل‌ترجمه که از لایهٔ UI داده می‌شوند. */
data class OptionalReceiptMessages(
    val empty: String,
    val invalid: String,
    val lowerGreaterThanUpper: String,
    val success: String,
    /** %1$s = پیام/کد خطای سوئیچ */
    val failed: String,
    val noHostValues: String,
    val unsupported: String,
)

/**
 * کف/سقف رسید اختیاری: کاربر مقادیر را وارد می‌کند، تراکنش 0800 (FC 034) ارسال می‌شود.
 * موفق → مقادیر برگشتی سوئیچ (FC 033) ذخیره و نمایش داده می‌شود.
 * ناموفق → همان مقادیر قبل از تغییر کاربر برمی‌گردد و پیام خطا نمایش داده می‌شود.
 */
@HiltViewModel
class OptionalReceiptSettingsViewModel @Inject constructor(
    private val pspGateway: PspGateway,
    private val store: OptionalReceiptStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(stateFrom(store.get()))
    val uiState: StateFlow<OptionalReceiptSettingsUiState> = _uiState.asStateFlow()

    fun onLowerChange(value: String) {
        _uiState.update { it.copy(lowerInput = value.filter(Char::isDigit).take(12), lowerError = null) }
    }

    fun onUpperChange(value: String) {
        _uiState.update { it.copy(upperInput = value.filter(Char::isDigit).take(12), upperError = null) }
    }

    fun save(messages: OptionalReceiptMessages) {
        val state = _uiState.value
        if (state.inProgress) return
        when (OptionalReceiptRules.validate(state.lowerInput, state.upperInput)) {
            OptionalReceiptValidationError.EMPTY -> {
                _uiState.update {
                    it.copy(
                        lowerError = messages.empty.takeIf { _ -> state.lowerInput.isBlank() },
                        upperError = messages.empty.takeIf { _ -> state.upperInput.isBlank() },
                    )
                }
                return
            }
            OptionalReceiptValidationError.INVALID -> {
                _uiState.update { it.copy(lowerError = messages.invalid) }
                return
            }
            OptionalReceiptValidationError.LOWER_GREATER_THAN_UPPER -> {
                _uiState.update { it.copy(upperError = messages.lowerGreaterThanUpper) }
                return
            }
            null -> Unit
        }
        val requested = OptionalReceiptLimits(
            active = true,
            lowerRials = state.lowerInput.toLong(),
            upperRials = state.upperInput.toLong(),
        )
        _uiState.update { it.copy(inProgress = true, resultMessage = null) }
        viewModelScope.launch {
            val result = runCatching { pspGateway.updateOptionalReceipt(requested) }.getOrNull()
            val applied = result?.limits
            if (result != null && result.isSuccess && applied != null) {
                store.save(applied)
                _uiState.value = stateFrom(applied).copy(
                    resultMessage = messages.success,
                    resultSuccess = true,
                )
            } else {
                val reason = when {
                    result == null -> messages.failed.format("")
                    result.unsupported -> messages.unsupported
                    result.isSuccess -> messages.noHostValues
                    else -> messages.failed.format(
                        listOf(result.responseMessage, result.responseCode)
                            .filter { it.isNotBlank() }
                            .joinToString(" - "),
                    )
                }
                // مقادیر قبل از تغییر کاربر حفظ می‌شود.
                _uiState.value = stateFrom(store.get()).copy(
                    resultMessage = reason.trim(),
                    resultSuccess = false,
                )
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(resultMessage = null) }
    }

    private fun stateFrom(limits: OptionalReceiptLimits?): OptionalReceiptSettingsUiState =
        OptionalReceiptSettingsUiState(
            lowerInput = limits?.takeIf { it.active }?.lowerRials?.toString().orEmpty(),
            upperInput = limits?.takeIf { it.active }?.upperRials?.toString().orEmpty(),
        )
}
