package com.danesh.common.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionContextProvider
import com.danesh.common.R
import com.danesh.common.card.CardSession
import com.danesh.common.card.ContactlessReadRequest
import com.danesh.common.card.KahrobaPolicy
import com.danesh.common.card.kahrobaPinRequired
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import com.danesh.core.emv.KahrobaEmvKernel
import com.danesh.core.emv.KahrobaEmvParams
import com.danesh.core.emv.KahrobaTransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

enum class SwipeCardStatus {
    Waiting,
    Reading,
    /** کارت کهربا شناسایی شده و جریان EMV در حال اجراست. */
    KahrobaReading,
    Error,
}

data class SwipeCardUiState(
    val status: SwipeCardStatus = SwipeCardStatus.Waiting,
    val errorMessage: String? = null,
    /** کهربا (NFC) در کنار کارت مغناطیسی فعال است. */
    val contactlessEnabled: Boolean = false,
)

sealed interface SwipeCardEvent {
    data class CardRead(val track2: String,val pan: String) : SwipeCardEvent
    data object Timeout : SwipeCardEvent
}

private const val KAHROBA_TAG = "Kahroba"
private const val CONTACTLESS_POLL_INTERVAL_MS = 300L

@HiltViewModel
class SwipeCardViewModel @Inject constructor(
    private val device: Device,
    private val cardSession: CardSession,
    private val kahrobaPolicy: KahrobaPolicy,
    private val contextProvider: TransactionContextProvider,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipeCardUiState())
    val uiState: StateFlow<SwipeCardUiState> = _uiState.asStateFlow()

    private val _events = Channel<SwipeCardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var readJob: Job? = null
    private var contactlessJob: Job? = null

    /** اولین منبعی که کارت را تحویل دهد (مغناطیسی یا کهربا) بقیه را بی‌اثر می‌کند. */
    private var cardHandled = AtomicBoolean(false)

    /** درخواست کهربای صفحه؛ null یعنی فقط کارت مغناطیسی (رفتار قبلی). */
    private var contactlessRequest: ContactlessReadRequest? = null

    private val contactlessActive: Boolean
        get() = contactlessRequest != null && kahrobaPolicy.isEnabled && device.supportsContactless

    fun startReadCard(contactless: ContactlessReadRequest? = null) {
        if (contactless != null) contactlessRequest = contactless
        if (readJob?.isActive == true || contactlessJob?.isActive == true) return

        val handled = AtomicBoolean(false)
        cardHandled = handled
        val nfcEnabled = contactlessActive

        _uiState.update {
            SwipeCardUiState(status = SwipeCardStatus.Reading, contactlessEnabled = nfcEnabled)
        }

        if (nfcEnabled) {
            contactlessJob = viewModelScope.launch(Dispatchers.IO) {
                pollContactless(contactlessRequest!!, handled)
            }
        }

        readJob = viewModelScope.launch {
            DeviceTrace.step("UI", "readCard started contactless=$nfcEnabled")
            device.readCard(
                context = context,
                onSuccess = { track2,pan ->
                    if (!handled.compareAndSet(false, true)) return@readCard
                    stopContactless()
                    viewModelScope.launch {
                        cardSession.clear()
                        cardSession.set(track2, pan)
                        _uiState.update { it.copy(status = SwipeCardStatus.Waiting, errorMessage = null) }
                        _events.send(SwipeCardEvent.CardRead(track2 = track2, pan = pan))
                    }
                },
                onError = { message ->
                    if (!handled.compareAndSet(false, true)) return@readCard
                    stopContactless()
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(
                                status = SwipeCardStatus.Error,
                                errorMessage = message,
                            )
                        }
                    }
                },
                onTimeOut = {
                    if (!handled.compareAndSet(false, true)) return@readCard
                    stopContactless()
                    viewModelScope.launch {
                        _uiState.update { it.copy(status = SwipeCardStatus.Waiting) }
                        _events.send(SwipeCardEvent.Timeout)
                    }
                },
            )
        }
    }

    /**
     * حلقه‌ی شناسایی کارت کهربا هم‌زمان با انتظار برای کشیدن کارت (مثل waitForCard در kahroba.c).
     */
    private suspend fun pollContactless(request: ContactlessReadRequest, handled: AtomicBoolean) {
        var cardTaken = false
        try {
            while (kotlinx.coroutines.currentCoroutineContext().isActive && !handled.get()) {
                if (device.detectContactlessCard()) {
                    if (!handled.compareAndSet(false, true)) return
                    cardTaken = true
                    device.stopReadCard()
                    readKahrobaCard(request)
                    return
                }
                delay(CONTACTLESS_POLL_INTERVAL_MS)
            }
        } finally {
            if (!cardTaken) device.closeContactless()
        }
    }

    private suspend fun readKahrobaCard(request: ContactlessReadRequest) {
        _uiState.update { it.copy(status = SwipeCardStatus.KahrobaReading, errorMessage = null) }
        val params = KahrobaEmvParams(
            transactionType = when (request) {
                is ContactlessReadRequest.Purchase -> KahrobaTransactionType.PURCHASE
                ContactlessReadRequest.Balance -> KahrobaTransactionType.BALANCE
            },
            amount = (request as? ContactlessReadRequest.Purchase)?.amount.orEmpty(),
            merchantName = merchantNameForCard(),
        )
        val result = try {
            runCatching {
                KahrobaEmvKernel(
                    transceive = { apdu -> device.transmitContactless(apdu) },
                    log = { DeviceTrace.debug(KAHROBA_TAG, it) },
                ).run(params)
            }
        } finally {
            device.closeContactless()
        }

        result.onSuccess { card ->
            val pinRequired = kahrobaPinRequired(
                request = request,
                cardRequestsPin = card.cardRequestsPin,
                noPinAmountLimit = kahrobaPolicy.noPinAmountLimit,
            )
            DeviceTrace.debug(
                KAHROBA_TAG,
                "card read panLen=${card.pan.length} cardPin=${card.cardRequestsPin} pinRequired=$pinRequired",
            )
            cardSession.setKahroba(card, pinRequired)
            _uiState.update { it.copy(status = SwipeCardStatus.Waiting) }
            _events.send(SwipeCardEvent.CardRead(track2 = card.track2, pan = card.pan))
        }.onFailure { error ->
            DeviceTrace.error(KAHROBA_TAG, "EMV flow failed: ${error.message}", error)
            _uiState.update {
                it.copy(
                    status = SwipeCardStatus.Error,
                    errorMessage = context.getString(R.string.kahroba_read_error),
                )
            }
        }
    }

    /** تگ 9F4E فقط ASCII می‌پذیرد؛ نام انگلیسی پذیرنده. */
    private fun merchantNameForCard(): String =
        runCatching { contextProvider.getTerminalConfig().englishMerchantName }
            .getOrDefault("")
            .filter { it.code in 0x20..0x7E }

    private fun stopContactless() {
        contactlessJob?.cancel()
        contactlessJob = null
    }

    fun retryReadCard() {
        cancelReading()
        startReadCard()
    }

    fun cancelReading() {
        cardHandled.set(true)
        stopContactless()
        readJob?.cancel()
        readJob = null
    }

    fun clearCardData() {
        cardSession.clear()
        cancelReading()
    }

    override fun onCleared() {
        cancelReading()
        super.onCleared()
    }
}
