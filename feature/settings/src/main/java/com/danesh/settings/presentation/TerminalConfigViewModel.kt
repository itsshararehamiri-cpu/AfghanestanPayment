package com.danesh.settings.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.PspGateway
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.settings.R
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.InitialConfigurationUiState
import com.danesh.settings.model.TerminalSetupPhase
import com.danesh.settings.receipt.InitialConfigurationReceiptBitmapFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "HpTerminalConfig"

/**
 * همراه‌پی: ردیف «پیکربندی پایانه» در صفحه پیکربندی — به‌جای inject کلید،
 * pspGateway.terminalConfig را صدا می‌زند و مشابه رسید راه‌اندازی به‌پرداخت،
 * موفق/ناموفق بودن و مشخصات پایانه (سریال، شماره پایانه/پذیرنده، نسخه برنامه) را
 * نمایش و چاپ می‌کند.
 */
@HiltViewModel
class TerminalConfigViewModel @Inject constructor(
    private val pspGateway: PspGateway,
    private val device: Device,
    private val contextProvider: TransactionContextProvider,
    private val appVersionProvider: AppVersionProvider,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val localePreferences: LocalePreferences,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InitialConfigurationUiState(phase = TerminalSetupPhase.EXECUTE_FORM),
    )
    val uiState: StateFlow<InitialConfigurationUiState> = _uiState.asStateFlow()

    fun confirm() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resultMessage = null) }
            Log.i(TAG, "UI | شروع پیکربندی پایانه (terminalConfig)")

            val result = runCatching { pspGateway.terminalConfig(TerminalConfigInput("")) }
                .getOrElse { throwable ->
                    Log.e(TAG, "UI | خطا در پیکربندی پایانه", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMessage = throwable.message.orEmpty().ifBlank {
                                appContext.getString(R.string.settings_initial_configuration_error_generic)
                            },
                        )
                    }
                    return@launch
                }

            Log.i(TAG, "UI | نتیجه پیکربندی پایانه: success=${result.isSuccess}")
            if (result.isSuccess) {
                val summary = buildConfigurationSummary()
                printConfigurationReceipt(summary)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        resultMessage = appContext.getString(R.string.settings_terminal_setup_success),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = result.responseMessage.ifBlank {
                            appContext.getString(R.string.settings_initial_configuration_error_failed)
                        },
                    )
                }
            }
        }
    }

    fun dismissSummary() {
        _uiState.update { it.copy(summary = null, resultMessage = null) }
    }

    private fun buildConfigurationSummary(): InitialConfigurationSummary {
        val serial = runCatching { device.getSerial() }.getOrElse { "" }
        val terminalConfig = contextProvider.getTerminalConfig()
        val appVersion = appVersionProvider.versionName()
        val programDate = ReceiptNowFormatter.format(localePreferences)
        return InitialConfigurationSummary(
            hardwareSerial = serial.ifBlank { "-" },
            terminalId = terminalConfig.terminalId.ifBlank { "-" },
            appVersion = appVersion.ifBlank { "-" },
            programDate = programDate,
            merchantId = terminalConfig.merchantId.ifBlank { "-" },
        )
    }

    private suspend fun printConfigurationReceipt(summary: InitialConfigurationSummary) {
        val bitmap = InitialConfigurationReceiptBitmapFactory.create(
            title = appContext.getString(R.string.settings_initial_configuration_receipt_title),
            hardwareSerialLabel = appContext.getString(R.string.settings_initial_configuration_hardware_serial),
            hardwareSerial = summary.hardwareSerial,
            terminalIdLabel = appContext.getString(R.string.settings_initial_configuration_terminal_id),
            terminalId = summary.terminalId,
            merchantIdLabel = appContext.getString(R.string.settings_initial_configuration_merchant_id),
            merchantId = summary.merchantId,
            appVersionLabel = appContext.getString(R.string.settings_initial_configuration_app_version),
            appVersion = summary.appVersion,
            programDateLabel = appContext.getString(R.string.settings_initial_configuration_program_date),
            programDate = summary.programDate,
            titleTypeface = paperReceiptTypefaceResolver.titleTypeface(appContext),
            lineTypeface = paperReceiptTypefaceResolver.bodyTypeface(appContext),
            lineTextAlign = paperReceiptTypefaceResolver.lineTextAlign(),
            lineX = paperReceiptTypefaceResolver.lineX(
                width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
            ),
        )
        printBitmap(
            bitmap,
            appContext.getString(R.string.settings_initial_configuration_receipt_print_label),
        )
    }

    private suspend fun printBitmap(bitmap: Bitmap, logLabel: String) {
        suspendCancellableCoroutine { continuation ->
            val job = CoroutineScope(continuation.context).launch {
                device.print(
                    bitmap = bitmap,
                    context = appContext,
                    onSuccess = {
                        Log.i(TAG, "UI | $logLabel چاپ شد")
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    },
                    onFailed = { message ->
                        Log.w(TAG, "UI | خطا در چاپ $logLabel: $message")
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    },
                )
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }
}
