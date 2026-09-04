package com.danesh.settings.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.InitialConfigurationPolicy
import com.danesh.api.PspGateway
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.common.receipt.ReceiptPspBrandProvider
import com.danesh.common.receipt.paper.PaperReceiptTypefaceResolver
import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.settings.R
import com.danesh.settings.domain.toDeviceConfigurationSummary
import com.danesh.settings.model.InitialConfigurationSummary
import com.danesh.settings.model.InitialConfigurationUiState
import com.danesh.settings.model.TerminalSetupPhase
import com.danesh.settings.receipt.InitialConfigurationReceiptBitmapFactory
import com.danesh.settings.receipt.KcvReceiptBitmapFactory
import com.danesh.settings.util.SettingsTextInputFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.danesh.common.locale.LocalePreferences
import com.danesh.common.locale.ReceiptNowFormatter
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "BpInit"

@HiltViewModel
class InitialConfigurationViewModel @Inject constructor(
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
init {
    viewModelScope.launch {
        Log.d(TAG, "nbnnnnnnnnnnnnnnnnn: ")
        initialConfigurationPolicy.injectKeys()
    }
}
    private val _uiState = MutableStateFlow(
        InitialConfigurationUiState(
            usesLogonSetup = initialConfigurationPolicy.usesTerminalSetupLogon,
            phase = if (configurationStore.isConfigured()) {
                TerminalSetupPhase.ACTION_CHOOSER
            } else {
                TerminalSetupPhase.EXECUTE_FORM
            },
        ),
    )
    val uiState: StateFlow<InitialConfigurationUiState> = _uiState.asStateFlow()

    fun updateFirstBallotTicket(ticket: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                firstBallotTicket = SettingsTextInputFilters.ballotTicketDigitsOnly(ticket),
                resultMessage = null,
            )
        }
    }

    fun updateSecondBallotTicket(ticket: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                secondBallotTicket = SettingsTextInputFilters.ballotTicketDigitsOnly(ticket),
                resultMessage = null,
            )
        }
    }

    fun scanFirstBallotTicket() = scanTicket(::updateFirstBallotTicket)

    fun scanSecondBallotTicket() = scanTicket(::updateSecondBallotTicket)

    private fun scanTicket(onTicket: (String) -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            device.scan(
                context = appContext,
                onSuccess = onTicket,
                onError = { message ->
                    _uiState.update { it.copy(resultMessage = message) }
                },
                onTimeout = {
                    _uiState.update {
                        it.copy(
                            resultMessage = appContext.getString(
                                R.string.settings_initial_configuration_qr_scan_timeout,
                            ),
                        )
                    }
                },
                onCancel = {},
            )
        }
    }

    fun confirm() {
        if (_uiState.value.isLoading) return
        when {
            initialConfigurationPolicy.usesTerminalSetupLogon -> runLogonSetup()
            else -> runDirectKeyInjection()
        }
    }

    private fun runLogonSetup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resultMessage = null) }
            Log.i(TAG, "UI | شروع logon (FirstInit) — فقط inject کلیدهای عملیاتی")
            completeLogonSetup()
        }
    }

    private suspend fun completeLogonSetup() {
        val wasConfigured = configurationStore.isConfigured()
        Log.d("TAG", "logon: dddddddddddddddddddddlenin1")
        val logonResult = runCatching { pspGateway.logon(masterKey = "") }.getOrElse { throwable ->
            Log.e(TAG, "UI | خطا در logon", throwable)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resultMessage = throwable.message.orEmpty().ifBlank {
                        appContext.getString(R.string.settings_initial_configuration_error_generic)
                    },
                )
            }
            return
        }

        Log.i(TAG, "UI | نتیجه logon: success=${logonResult.isSuccess} rc=${logonResult.responseCode}")
        if (logonResult.isSuccess) {
            val summary = buildConfigurationSummary()
            configurationStore.markConfigured()
            configurationStore.saveConfigurationSummary(summary.toDeviceConfigurationSummary())
            if (!wasConfigured) {
                printConfigurationReceipt(summary)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    summary = summary,
                    resultMessage = appContext.getString(R.string.settings_terminal_setup_success),
                    phase = if (!wasConfigured) {
                        TerminalSetupPhase.EXECUTE_FORM
                    } else {
                        TerminalSetupPhase.ACTION_CHOOSER
                    },
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resultMessage = logonResult.responseMessage.ifBlank {
                        appContext.getString(R.string.settings_initial_configuration_error_failed)
                    },
                )
            }
        }
    }

    private fun runDirectKeyInjection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resultMessage = null) }
            Log.i(TAG, "UI | شروع inject مستقیم کلید (HP)")

            val injectResult = runCatching { initialConfigurationPolicy.injectKeys() }
                .getOrElse { Result.failure(it) }

            injectResult.fold(
                onSuccess = {
                    Log.i(TAG, "UI | inject کلید موفق — چاپ رسید و KCV")
                    val summary = buildConfigurationSummary()
                    configurationStore.saveConfigurationSummary(summary.toDeviceConfigurationSummary())
                    printConfigurationReceipts(summary)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            resultMessage = null,
                            phase = TerminalSetupPhase.ACTION_CHOOSER,
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "UI | خطا در inject کلید", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resultMessage = error.message.orEmpty().ifBlank {
                                appContext.getString(
                                    R.string.settings_initial_configuration_error_key_inject,
                                )
                            },
                        )
                    }
                },
            )
        }
    }

    fun dismissSummary() {
        _uiState.update {
            it.copy(
                summary = null,
                phase = if (configurationStore.isConfigured()) {
                    TerminalSetupPhase.ACTION_CHOOSER
                } else {
                    TerminalSetupPhase.EXECUTE_FORM
                },
            )
        }
    }

    fun selectExecute() {
        _uiState.update {
            it.copy(
                phase = TerminalSetupPhase.EXECUTE_FORM,
                resultMessage = null,
            )
        }
    }

    fun printConfigurationReceiptOnly() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resultMessage = null) }
            val summary = buildConfigurationSummary()
            printConfigurationReceipt(summary)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resultMessage = appContext.getString(R.string.settings_terminal_setup_print_success),
                )
            }
        }
    }

    fun onBackFromSetup(): Boolean {
        return when {
            _uiState.value.summary != null -> {
                dismissSummary()
                false
            }
            _uiState.value.phase == TerminalSetupPhase.EXECUTE_FORM &&
                configurationStore.isConfigured() -> {
                _uiState.update {
                    it.copy(
                        phase = TerminalSetupPhase.ACTION_CHOOSER,
                        resultMessage = null,
                        firstBallotTicket = "",
                        secondBallotTicket = "",
                    )
                }
                false
            }
            else -> true
        }
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

    private suspend fun printConfigurationReceipts(summary: InitialConfigurationSummary) {
        printConfigurationReceipt(summary)
        printKcvReceipt()
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

    private suspend fun printKcvReceipt() {
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
