package com.danesh.settings.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.KeyCardKcvSummary
import com.danesh.api.KeyCardLoadingService
import com.danesh.api.KeyCardPinRejectedException
import com.danesh.api.KeyCardType
import com.danesh.settings.R
import com.danesh.settings.domain.TerminalStartupRunner
import com.danesh.settings.model.KeyLoadingKcvSummary
import com.danesh.settings.model.SadadKeyInjectionStep
import com.danesh.settings.model.SadadKeyInjectionUiState
import com.danesh.settings.receipt.SetupReceiptPrinter
import com.danesh.settings.util.SettingsTextInputFilter
import com.danesh.settings.util.SettingsTextInputFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val TAG = "SadadKeyInjection"
private const val POLL_INTERVAL_MS = 400L
private const val CARD_WAIT_TIMEOUT_MS = 60_000L
private const val SWAP_WAIT_TIMEOUT_MS = 120_000L
private const val REMOVE_WAIT_TIMEOUT_MS = 60_000L
private const val MAX_INDEX_LENGTH = 3
private const val MIN_PIN_LENGTH = 4
private const val MAX_PIN_LENGTH = 8

/**
 * سداد — «تزریق کلید»:
 * ۱. در انتظار کارت ICC  ۲. ایندکس و رمز کارت A و C  ۳. خواندن کارت A (یا کلید ذخیره‌شده)
 * ۴. خواندن کارت C و تزریق به PED (اگر C کارت جداست: خارج کردن A و وارد کردن C)
 * ۵. INIT و سپس LOGON خودکار  ۶. چاپ KCV و مشخصات پایانه/پذیرنده  ۷. «لطفاً کارت را خارج کنید».
 */
@HiltViewModel
class SadadKeyInjectionViewModel @Inject constructor(
    private val service: KeyCardLoadingService,
    private val startupRunner: TerminalStartupRunner,
    private val receiptPrinter: SetupReceiptPrinter,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SadadKeyInjectionUiState(
            cardAIndex = service.persistedRsaKeyIndex()?.toString().orEmpty(),
            cardCIndex = service.persistedCardCIndex()?.toString().orEmpty(),
        ),
    )
    val uiState: StateFlow<SadadKeyInjectionUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    init {
        waitForCard()
    }

    /** قابل فراخوانی از دکمه «تلاش مجدد» در صفحه انتظار کارت. */
    fun waitForCard() {
        activeJob?.cancel()
        activeJob = viewModelScope.launch { waitForCardLoop() }
    }

    private suspend fun waitForCardLoop() {
        try {
            waitForCardLoopInternal()
        } finally {
            closeCardReader()
        }
    }

    private suspend fun waitForCardLoopInternal() {
        _uiState.update {
            it.copy(
                step = SadadKeyInjectionStep.WAIT_CARD,
                statusMessage = string(R.string.settings_sadad_insert_card_message),
                waitFailedMessage = null,
                errorMessage = null,
            )
        }
        val detected = try {
            awaitCardState(present = true, timeoutMs = CARD_WAIT_TIMEOUT_MS)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Log.e(TAG, "card detection failed", error)
            _uiState.update {
                it.copy(waitFailedMessage = string(R.string.settings_sadad_card_detect_error))
            }
            return
        }
        if (detected) {
            _uiState.update {
                it.copy(
                    step = SadadKeyInjectionStep.FORM,
                    statusMessage = string(R.string.settings_sadad_card_detected),
                )
            }
        } else {
            _uiState.update {
                it.copy(waitFailedMessage = string(R.string.settings_sadad_card_detect_timeout))
            }
        }
    }

    fun updateCardAIndex(value: String) = updateForm {
        it.copy(cardAIndex = filterIndex(value), cardAIndexError = null)
    }

    fun updateCardCIndex(value: String) = updateForm {
        it.copy(cardCIndex = filterIndex(value), cardCIndexError = null)
    }

    fun updateCardAPin(value: String) = updateForm {
        it.copy(cardAPin = filterPin(value), cardAPinError = null)
    }

    fun updateCardCPin(value: String) = updateForm {
        it.copy(cardCPin = filterPin(value), cardCPinError = null)
    }

    fun submit() {
        val state = _uiState.value
        if (state.step != SadadKeyInjectionStep.FORM) return
        val indexA = validateIndex(state.cardAIndex, R.string.settings_sadad_error_index_a)
        val indexC = validateIndex(state.cardCIndex, R.string.settings_sadad_error_index_c)
        val pinAError = validatePin(state.cardAPin)
        val pinCError = validatePin(state.cardCPin)
        _uiState.update {
            it.copy(
                cardAIndexError = indexA.second,
                cardCIndexError = indexC.second,
                cardAPinError = pinAError,
                cardCPinError = pinCError,
            )
        }
        val cardAIndex = indexA.first ?: return
        val cardCIndex = indexC.first ?: return
        if (pinAError != null || pinCError != null) return

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            runInjection(
                cardAIndex = cardAIndex,
                cardCIndex = cardCIndex,
                pinA = state.cardAPin,
                pinC = state.cardCPin,
            )
        }
    }

    /** از صفحه خطا: اگر کارت هنوز داخل است به فرم برمی‌گردد، وگرنه دوباره منتظر کارت می‌ماند. */
    fun retry() {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            val present = runCatching { service.isCardPresent() }.getOrDefault(false)
            _uiState.update {
                it.copy(cardAPin = "", cardCPin = "", errorMessage = null, notes = emptyList())
            }
            if (present) {
                _uiState.update {
                    it.copy(
                        step = SadadKeyInjectionStep.FORM,
                        statusMessage = string(R.string.settings_sadad_card_detected),
                    )
                }
            } else {
                waitForCardLoop()
            }
        }
    }

    fun cancel() {
        activeJob?.cancel()
        activeJob = null
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }

    private suspend fun runInjection(cardAIndex: Int, cardCIndex: Int, pinA: String, pinC: String) {
        val notes = mutableListOf<String>()
        _uiState.update {
            it.copy(
                step = SadadKeyInjectionStep.PROCESSING,
                statusMessage = string(R.string.settings_sadad_step_checking_card),
                errorMessage = null,
                notes = emptyList(),
                kcv = null,
                initResult = null,
                logonResult = null,
                printErrorMessage = null,
                cardRemoved = false,
            )
        }
        try {
            if (!service.isCardPresent()) {
                fail(string(R.string.settings_sadad_error_card_removed))
                return
            }
            val hasCardA = service.hasApplet(KeyCardType.CARD_A)
            val hasCardC = service.hasApplet(KeyCardType.CARD_C)
            Log.i(TAG, "applets on inserted card: A=$hasCardA C=$hasCardC")
            if (!hasCardA && !hasCardC) {
                fail(string(R.string.settings_sadad_error_not_key_card))
                return
            }

            // مرحله ۱: کارت A
            if (hasCardA) {
                setStatus(R.string.settings_sadad_step_reading_a)
                val cardAResult = service.loadKeyPairFromCardA(pinA, cardAIndex)
                cardAResult.exceptionOrNull()?.let { error ->
                    fail(describeCardError(error, "A", R.string.settings_sadad_error_card_a))
                    return
                }
            } else if (service.hasStoredKeyPair(cardAIndex)) {
                notes += appContext.getString(R.string.settings_sadad_card_a_used_stored, cardAIndex.toString())
            } else {
                fail(appContext.getString(R.string.settings_sadad_error_no_card_a, cardAIndex.toString()))
                return
            }

            // مرحله ۲: کارت C (اگر روی همان کارت نیست، تعویض کارت)
            if (!hasCardC) {
                if (!swapToCardC()) return
            }
            setStatus(R.string.settings_sadad_step_reading_c)
            val kcv = service.loadAndInjectMasterKeys(
                card = KeyCardType.CARD_C,
                pin = pinC,
                keyIndex = cardCIndex,
                rsaKeyIndex = cardAIndex,
            ).getOrElse { error ->
                fail(describeCardError(error, "C", R.string.settings_sadad_error_card_c))
                return
            }.toUiSummary()
            _uiState.update { it.copy(kcv = kcv, notes = notes.toList()) }

            // مرحله ۳: INIT و سپس LOGON خودکار
            setStatus(R.string.settings_sadad_step_init)
            val initResult = startupRunner.runInit()
            _uiState.update { it.copy(initResult = initResult) }
            if (initResult.isSuccess) {
                setStatus(R.string.settings_sadad_step_logon)
                val logonResult = startupRunner.runLogon()
                _uiState.update { it.copy(logonResult = logonResult) }
            }

            // مرحله ۴: چاپ KCV و مشخصات پایانه/پذیرنده (بعد از LOGON که شماره‌ها به‌روز شده‌اند)
            setStatus(R.string.settings_sadad_step_printing)
            val printError = receiptPrinter.printKcvReceipt(kcv)
                ?: receiptPrinter.printConfigurationReceipt(receiptPrinter.buildConfigurationSummary())
            _uiState.update {
                it.copy(
                    step = SadadKeyInjectionStep.SUCCESS,
                    statusMessage = string(R.string.settings_sadad_remove_card),
                    printErrorMessage = printError?.let { message ->
                        appContext.getString(R.string.settings_sadad_print_failed, message)
                    },
                )
            }

            // مرحله ۵: «لطفاً کارت را خارج کنید» — polling محدود؛ قبلاً بی‌نهایت ادامه داشت
            // و حتی بعد از رفتن به صفحهٔ دیگر هر ۴۰۰ms کارت‌خوان را باز/بررسی می‌کرد.
            if (awaitCardState(present = false, timeoutMs = REMOVE_WAIT_TIMEOUT_MS)) {
                _uiState.update {
                    it.copy(
                        cardRemoved = true,
                        statusMessage = string(R.string.settings_sadad_card_removed_done),
                    )
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Log.e(TAG, "key injection failed", error)
            fail(unexpectedErrorMessage(error))
        } finally {
            closeCardReader()
        }
    }

    /** بعد از اتمام کار با کارت ICC (موفق، خطا یا لغو) کارت‌خوان بسته می‌شود. */
    private suspend fun closeCardReader() {
        withContext(NonCancellable) {
            runCatching { service.releaseCardReader() }
                .onFailure { Log.w(TAG, "closing card reader failed", it) }
        }
    }

    /** کارت A خارج و کارت C وارد شود. false یعنی خطا نمایش داده شده است. */
    private suspend fun swapToCardC(): Boolean {
        _uiState.update {
            it.copy(
                step = SadadKeyInjectionStep.SWAP_CARD,
                swapCardRemoved = false,
                statusMessage = string(R.string.settings_sadad_swap_remove_a),
            )
        }
        if (!awaitCardState(present = false, timeoutMs = SWAP_WAIT_TIMEOUT_MS)) {
            fail(string(R.string.settings_sadad_card_wait_timeout_swap))
            return false
        }
        _uiState.update {
            it.copy(swapCardRemoved = true, statusMessage = string(R.string.settings_sadad_swap_insert_c))
        }
        if (!awaitCardState(present = true, timeoutMs = SWAP_WAIT_TIMEOUT_MS)) {
            fail(string(R.string.settings_sadad_card_wait_timeout_swap))
            return false
        }
        // فرصت کوتاه برای جا افتادن کامل کارت در کارت‌خوان
        delay(POLL_INTERVAL_MS)
        _uiState.update {
            it.copy(
                step = SadadKeyInjectionStep.PROCESSING,
                statusMessage = string(R.string.settings_sadad_step_checking_card),
            )
        }
        if (!service.hasApplet(KeyCardType.CARD_C)) {
            fail(string(R.string.settings_sadad_error_not_card_c))
            return false
        }
        return true
    }

    /** true وقتی وضعیت کارت به [present] رسید؛ false در صورت پایان زمان. */
    private suspend fun awaitCardState(present: Boolean, timeoutMs: Long): Boolean =
        withTimeoutOrNull(timeoutMs) {
            while (service.isCardPresent() != present) {
                delay(POLL_INTERVAL_MS)
            }
            true
        } ?: false

    private suspend fun describeCardError(error: Throwable, cardName: String, wrapper: Int): String {
        if (error is KeyCardPinRejectedException) {
            return if (error.remainingTries <= 0) {
                appContext.getString(R.string.settings_sadad_error_pin_blocked, cardName)
            } else {
                appContext.getString(R.string.settings_sadad_error_pin_rejected, cardName, error.remainingTries)
            }
        }
        val cardStillPresent = runCatching { service.isCardPresent() }.getOrDefault(false)
        if (!cardStillPresent) return string(R.string.settings_sadad_error_card_removed)
        return appContext.getString(wrapper, unexpectedErrorMessage(error))
    }

    private fun unexpectedErrorMessage(error: Throwable): String =
        error.message?.takeIf { it.isNotBlank() } ?: string(R.string.settings_sadad_failure_unknown)

    private fun fail(message: String) {
        _uiState.update {
            it.copy(
                step = SadadKeyInjectionStep.ERROR,
                errorMessage = message,
                statusMessage = null,
                cardAPin = "",
                cardCPin = "",
            )
        }
    }

    private fun setStatus(resId: Int) {
        _uiState.update { it.copy(statusMessage = string(resId)) }
    }

    private fun updateForm(transform: (SadadKeyInjectionUiState) -> SadadKeyInjectionUiState) {
        if (_uiState.value.step != SadadKeyInjectionStep.FORM) return
        _uiState.update(transform)
    }

    private fun validateIndex(value: String, emptyError: Int): Pair<Int?, String?> {
        if (value.isBlank()) return null to string(emptyError)
        val index = value.toIntOrNull()
        if (index == null || index !in 1..999) return null to string(R.string.settings_sadad_error_index_range)
        return index to null
    }

    private fun validatePin(value: String): String? =
        if (value.length < MIN_PIN_LENGTH) string(R.string.settings_sadad_error_pin) else null

    private fun filterIndex(value: String): String =
        SettingsTextInputFilters.apply(SettingsTextInputFilter.PositiveInteger, value).take(MAX_INDEX_LENGTH)

    private fun filterPin(value: String): String =
        SettingsTextInputFilters.apply(SettingsTextInputFilter.DigitsOnly, value).take(MAX_PIN_LENGTH)

    private fun string(resId: Int): String = appContext.getString(resId)

    private fun KeyCardKcvSummary.toUiSummary(): KeyLoadingKcvSummary {
        val notSet = string(R.string.settings_kcv_value_not_set)
        return KeyLoadingKcvSummary(
            master = terminalMasterKey.ifBlank { notSet },
            mac = mac.ifBlank { notSet },
            pin = pin.ifBlank { notSet },
            data = data.ifBlank { notSet },
        )
    }
}
