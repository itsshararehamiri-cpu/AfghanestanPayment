package com.danesh.common.pin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class GetPinStatus {
    Waiting,
    Reading,
    Processing,
    Error,
}

data class GetPinUiState(
    val status: GetPinStatus = GetPinStatus.Waiting,
    val pinLength: Int = 0,
    val pinValue: String = "",
    val errorMessage: String? = null,
    val useDevicePinPad: Boolean = true,
    val response: String = "",
    val isSuccessful: Boolean = false,
    val isUnSuccessful: Boolean = false,
    val connectionError: Boolean = false,
)

sealed interface GetPinEvent {
    data class PinBlockReady(val pinBlock: String) : GetPinEvent
    data class TransactionSuccess(val response: String) : GetPinEvent
    data class TransactionFailure(val response: String) : GetPinEvent
    data object Timeout : GetPinEvent
    data object Cancelled : GetPinEvent
}

interface GetPinContract {
    val uiState: StateFlow<GetPinUiState>
    val events: Flow<GetPinEvent>

    fun setCardData(track2: String,pan: String)
    fun clearCardData()
    fun startPinEntry()
    fun onSoftPinChange(value: String)
    fun retryPinEntry()
    fun cancelPinEntry()
}
