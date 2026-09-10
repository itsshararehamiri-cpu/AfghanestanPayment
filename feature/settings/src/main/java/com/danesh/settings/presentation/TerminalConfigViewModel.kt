package com.danesh.settings.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspGateway
import com.danesh.api.TerminalConfigInput
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.settings.R
import com.danesh.settings.domain.toDeviceConfigurationSummary
import com.danesh.settings.model.HpTerminalProvisioningUiState
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.receipt.InitialConfigurationReceiptBitmapFactory
import com.danesh.settings.receipt.KcvReceiptBitmapFactory
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
 * همراه‌پی: صفحه «پیکربندی پایانه» در منوی پیکربندی — هم «کلیدگذاری» (inject مستقیم
 * کلید) و هم «دریافت اطلاعات پایانه» (pspGateway.terminalConfig) در همین صفحه انجام
 * می‌شوند؛ بدون جابه‌جایی به صفحه دیگر، نتیجه/KCV/مشخصات پایانه درون همان بخش نمایش
 * داده و — مشابه رسید راه‌اندازی به‌پرداخت — چاپ می‌شوند.
 */
@HiltViewModel
class TerminalConfigViewModel @Inject constructor(
    private val pspGateway: PspGateway,
    private val initialConfigurationPolicy: InitialConfigurationPolicy,
    private val device: Device,
    private val configurationStore: DeviceConfigurationStore,
    private val contextProvider: TransactionContextProvider,
    private val appVersionProvider: AppVersionProvider,
    private val paperReceiptTypefaceResolver: PaperReceiptTypefaceResolver,
    private val receiptPspBrandProvider: ReceiptPspBrandProvider,
    private val localePreferences: LocalePreferences,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HpTerminalProvisioningUiState())
    val uiState: StateFlow<HpTerminalProvisioningUiState> = _uiState.asStateFlow()

    fun confirmKeyLoading() {
        if (_uiState.value.keyLoading.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    keyLoading = it.keyLoading.copy(
                        isLoading = true,
                        resultMessage = null,
                        isSuccess = false,
                        kcvSummary = null,
                    ),
                )
            }
            Log.i(TAG, "UI | شروع inject مستقیم کلید (HP)")

            val injectResult = runCatching { initialConfigurationPolicy.injectKeys() }
                .getOrElse { Result.failure(it) }

            injectResult.fold(
                onSuccess = {
                    Log.i(TAG, "UI | inject کلید موفق — چاپ رسید و KCV")
                    val summary = buildConfigurationSummary()
                    configurationStore.saveConfigurationSummary(summary.toDeviceConfigurationSummary())
                    printConfigurationReceipt(summary)
                    val kcvSummary = printKcvReceiptAndBuildSummary()
                    _uiState.update {
                        it.copy(
                            keyLoading = it.keyLoading.copy(
                                isLoading = false,
                                isSuccess = true,
                                kcvSummary = kcvSummary,
                                resultMessage = appContext.getString(R.string.settings_key_loading_success),
                            ),
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "UI | خطا در inject کلید", error)
                    _uiState.update {
                        it.copy(
                            keyLoading = it.keyLoading.copy(
                                isLoading = false,
                                isSuccess = false,
                                resultMessage = error.message.orEmpty().ifBlank {
                                    appContext.getString(R.string.settings_initial_configuration_error_key_inject)
                                },
                            ),
                        )
                    }
                },
            )
        }
    }

    fun confirmTerminalInfo() {
        if (_uiState.value.terminalInfo.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    terminalInfo = it.terminalInfo.copy(
                        isLoading = true,
                        resultMessage = null,
                        isSuccess = false,
                    ),
                )
            }
            Log.i(TAG, "UI | شروع پیکربندی پایانه (terminalConfig)")

            val result = runCatching { pspGateway.terminalConfig(TerminalConfigInput("")) }
                .getOrElse { throwable ->
                    Log.e(TAG, "UI | خطا در پیکربندی پایانه", throwable)
                    _uiState.update {
                        it.copy(
                            terminalInfo = it.terminalInfo.copy(
                                isLoading = false,
                                resultMessage = throwable.message.orEmpty().ifBlank {
                                    appContext.getString(R.string.settings_initial_configuration_error_generic)
                                },
                            ),
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
                        terminalInfo = it.terminalInfo.copy(
                            isLoading = false,
                            isSuccess = true,
                            summary = summary,
                            resultMessage = appContext.getString(R.string.settings_terminal_setup_success),
                        ),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        terminalInfo = it.terminalInfo.copy(
                            isLoading = false,
                            isSuccess = false,
                            resultMessage = result.responseMessage.ifBlank {
                                appContext.getString(R.string.settings_initial_configuration_error_failed)
                            },
                        ),
                    )
                }
            }
        }
    }

    /** پس از دریافت موفق اطلاعات پایانه، مقادیر تازه (DE41/DE42/DE43) نزد [contextProvider]
     * ذخیره شده‌اند (رجوع کنید به TerminalConfigHandler) — این تابع همان مقادیر ذخیره‌شده
     * را برای نمایش/چاپ می‌خواند.
     */
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

    private suspend fun printKcvReceiptAndBuildSummary(): KeyLoadingKcvSummary {
        val kcv = runCatching { device.getKCv() }.getOrElse { KCV("", "", "", "") }
        val bitmap = KcvReceiptBitmapFactory.create(
            context = appContext,
            title = appContext.getString(R.string.settings_kcv_receipt_title),
            macKeyLabel = appContext.getString(R.string.settings_kcv_mac_key),
            macKeyValue = kcv.mac.ifBlank { "-" },
            dataKeyLabel = appContext.getString(R.string.settings_kcv_data_key),
            dataKeyValue = kcv.data.ifBlank { "-" },
            pinKeyLabel = appContext.getString(R.string.settings_kcv_pin_key),
            pinKeyValue = kcv.pin.ifBlank { "-" },
            fonts = paperReceiptTypefaceResolver.bitmapFonts(
                context = appContext,
                width = InitialConfigurationReceiptBitmapFactory.WIDTH,
                horizontalPadding = InitialConfigurationReceiptBitmapFactory.HORIZONTAL_PADDING,
            ),
            pspBrand = receiptPspBrandProvider.current(),
        )
        printBitmap(
            bitmap,
            appContext.getString(R.string.settings_initial_configuration_kcv_receipt_print_label),
        )
        return KeyLoadingKcvSummary(
            master = kcv.master.ifBlank { "-" },
            mac = kcv.mac.ifBlank { "-" },
            pin = kcv.pin.ifBlank { "-" },
            data = kcv.data.ifBlank { "-" },
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
