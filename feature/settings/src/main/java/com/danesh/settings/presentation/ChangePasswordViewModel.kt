package com.danesh.settings.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.settings.data.SettingsPasswordRepository
import com.danesh.settings.domain.MerchantPasswordValidator
import com.danesh.settings.receipt.ChangePasswordReceiptBitmapFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ChangePasswordUiState(
    val newPasswordError: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val passwordRepository: SettingsPasswordRepository,
    private val device: Device,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val localePreferences: LocalePreferences,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(
        newPassword: String,
        insecurePasswordError: String,
    ) {
        viewModelScope.launch {
            if (!MerchantPasswordValidator.isSecure(newPassword)) {
                _uiState.update {
                    it.copy(newPasswordError = insecurePasswordError, isSuccess = false)
                }
                return@launch
            }
            passwordRepository.updateMerchantPassword(newPassword)
            printChangePasswordReceipt()
            _uiState.update {
                it.copy(newPasswordError = null, isSuccess = true)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(newPasswordError = null) }
    }

    private suspend fun printChangePasswordReceipt() {
        val dateTime = ReceiptNowFormatter.format(localePreferences)
        val bitmap = ChangePasswordReceiptBitmapFactory.create(
            title = appContext.getString(com.danesh.settings.R.string.settings_change_password_receipt_title),
            dateTimeLabel = appContext.getString(com.danesh.settings.R.string.settings_change_password_receipt_date_time),
            dateTime = dateTime,
            successLabel = appContext.getString(com.danesh.settings.R.string.settings_change_password_receipt_success),
            fonts = paperReceiptTypefaceResolver.bitmapFonts(
                context = appContext,
                width = 384,
                horizontalPadding = 16,
            ),
        )
        printBitmap(bitmap)
    }

    private suspend fun printBitmap(bitmap: android.graphics.Bitmap) {
        suspendCancellableCoroutine { continuation ->
            val job = CoroutineScope(continuation.context).launch {
                device.print(
                    bitmap = bitmap,
                    context = appContext,
                    onSuccess = {
                        if (continuation.isActive) continuation.resume(Unit)
                    },
                    onFailed = { message ->
                        Log.w(TAG, "change password receipt print failed: $message")
                        if (continuation.isActive) continuation.resume(Unit)
                    },
                )
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }

    companion object {
        private const val TAG = "ChangePasswordViewModel"
    }
}
