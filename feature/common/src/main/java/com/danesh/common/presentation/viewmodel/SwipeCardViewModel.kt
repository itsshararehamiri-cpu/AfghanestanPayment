package com.danesh.common.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.card.CardSession
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SwipeCardStatus {
    Waiting,
    Reading,
    Error,
}

data class SwipeCardUiState(
    val status: SwipeCardStatus = SwipeCardStatus.Waiting,
    val errorMessage: String? = null,
)

sealed interface SwipeCardEvent {
    data class CardRead(val track2: String,val pan: String) : SwipeCardEvent
    data object Timeout : SwipeCardEvent
}

@HiltViewModel
class SwipeCardViewModel @Inject constructor(
    private val device: Device,
    private val cardSession: CardSession,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipeCardUiState())
    val uiState: StateFlow<SwipeCardUiState> = _uiState.asStateFlow()

    private val _events = Channel<SwipeCardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var readJob: Job? = null

    fun startReadCard() {
        if (readJob?.isActive == true) return

        _uiState.update {
            SwipeCardUiState(status = SwipeCardStatus.Reading)
        }

        readJob = viewModelScope.launch {
            DeviceTrace.step("UI", "readCard started")
            device.readCard(
                context = context,
                onSuccess = { track2,pan ->
                    viewModelScope.launch {
                        cardSession.set(track2, pan)
                        _uiState.update { SwipeCardUiState(status = SwipeCardStatus.Waiting) }
                        _events.send(SwipeCardEvent.CardRead(track2 = track2, pan = pan))
                    }
                },
                onError = { message ->
                    viewModelScope.launch {
                        _uiState.update {
                            SwipeCardUiState(
                                status = SwipeCardStatus.Error,
                                errorMessage = message,
                            )
                        }
                    }
                },
                onTimeOut = {
                    viewModelScope.launch {
                        _uiState.update { SwipeCardUiState(status = SwipeCardStatus.Waiting) }
                        _events.send(SwipeCardEvent.Timeout)
                    }
                },
            )
        }
    }

    fun retryReadCard() {
        readJob?.cancel()
        readJob = null
        startReadCard()
    }

    fun cancelReading() {
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
