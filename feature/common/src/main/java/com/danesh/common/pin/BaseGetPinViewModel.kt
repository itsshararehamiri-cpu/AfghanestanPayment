package com.danesh.common.pin

//import android.content.Context
//import android.util.Logimport androidx.lifecycle.SavedStateHandle
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionTransportCodes
import com.danesh.api.resolvedSuccess
import com.danesh.api.toJson
import com.danesh.common.R
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.card.CardSession
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "Transaction"

abstract class BaseGetPinViewModel(
    savedStateHandle: SavedStateHandle,
    private val device: Device,
    private val cardSession: CardSession,
    @ApplicationContext private val context: Context,
) : ViewModel(), GetPinContract {

    protected var track2: String =
        savedStateHandle.get<String>(SwipeCardNavArgs.TRACK_2).orEmpty()
        private set

    protected var pan: String =
        savedStateHandle.get<String>(SwipeCardNavArgs.PAN).orEmpty()
        private set

    private val _uiState = MutableStateFlow(
        GetPinUiState(useDevicePinPad = !device.hasKeyboard),
    )

    private val _events = Channel<GetPinEvent>(Channel.BUFFERED)

    override val uiState: StateFlow<GetPinUiState> = _uiState.asStateFlow()
    override val events = _events.receiveAsFlow()

    private var pinJob: Job? = null
    private var isSubmitting = false

    protected abstract suspend fun executeTransaction(
        pinBlock: String,
        track2: String,pan: String
    ): TransactionResultDetail

    override fun setCardData(track2: String,pan: String) {
        if (this.track2.isEmpty()) {
            this.track2 = track2
        }
        if (this.pan.isEmpty()) {
            this.pan = pan
        }
        cardSession.set(this.track2, this.pan)
    }

    override fun clearCardData() {
        track2 = ""
        pan = ""
        cardSession.clear()
        cancelPinEntry()
    }

    override fun startPinEntry() {
        Log.d(TAG, "startPinEntry | pinJobActive=${pinJob?.isActive} | panLen=${pan.length}")
        if (pinJob?.isActive == true) return

        val pan =pan//,// CardTrackUtils.extractPan(cardData)
        if (pan.length < 16) {
            _uiState.update {
                GetPinUiState(
                    status = GetPinStatus.Error,
                    errorMessage = context.getString(R.string.error_invalid_card_number),
                    useDevicePinPad = !device.hasKeyboard,
                )
            }
            return
        }

        _uiState.update {
            GetPinUiState(
                status = GetPinStatus.Reading,
                useDevicePinPad = !device.hasKeyboard,
            )
        }

        if (device.hasKeyboard) {
            return
        }

        pinJob = viewModelScope.launch {
            DeviceTrace.step("UI", "getPinBlock started panLen=${pan.length}")
            device.getPinBlock(
                context = context,
                pan = pan,
                onError = { message ->
                    viewModelScope.launch {
                        _uiState.update {
                            GetPinUiState(
                                status = GetPinStatus.Error,
                                errorMessage = message,
                                useDevicePinPad = !device.hasKeyboard,
                            )
                        }
                    }
                },
                onInput = { length ->
                    viewModelScope.launch {
                        _uiState.update { state ->
                            state.copy(
                                pinLength = length.coerceIn(0, 4),
                                errorMessage = null,
                            )
                        }
                    }
                },
                onConfirm = { pinBlock ->
                    Log.d(TAG, "onConfirm | pinBlock received, submitting transaction")
                    submitPin(pinBlock)
                },
                onCancel = {
                    viewModelScope.launch {
                        if (!transactionCompleted) {
                            _events.send(GetPinEvent.Cancelled)
                        }
                    }
                },
                onTimeOut = {
                    viewModelScope.launch {
                        if (!transactionCompleted) {
                            _uiState.update { state ->
                                state.copy(
                                    status = GetPinStatus.Waiting,
                                    pinLength = 0,
                                    pinValue = "",
                                )
                            }
                            _events.send(GetPinEvent.Timeout)
                        }
                    }
                },
            )
        }
    }

    override fun onSoftPinChange(value: String) {
        if (_uiState.value.status == GetPinStatus.Processing) return
        if (_uiState.value.useDevicePinPad) return
        if (value.length > 4 || value.any { !it.isDigit() }) return

        _uiState.update { state ->
            state.copy(
                pinValue = value,
                pinLength = value.length,
                errorMessage = null,
            )
        }

        if (value.length == 4) {
            submitPin(value)
        }
    }

    override fun retryPinEntry() {
        pinJob?.cancel()
        pinJob = null
        isSubmitting = false
        _uiState.update {
            GetPinUiState(useDevicePinPad = !device.hasKeyboard)
        }
        startPinEntry()
    }

    override fun cancelPinEntry() {
        pinJob?.cancel()
        pinJob = null
        isSubmitting = false
    }

    override fun onCleared() {
        clearCardData()
        super.onCleared()
    }

    private var transactionCompleted = false

    private fun submitPin(pinBlock: String) {
        Log.d(TAG, "submitPin | isSubmitting=$isSubmitting")
        if (isSubmitting) return
        isSubmitting = true
        transactionCompleted = false
        pinJob?.cancel()
        pinJob = null
        Log.d(TAG, "submitPin | starting transaction execution")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = GetPinStatus.Processing,
                    errorMessage = null,
                    isSuccessful = false,
                    isUnSuccessful = false,
                )
            }
            try {
                val result = executeTransaction(pinBlock, track2,pan)
                val success = result.resolvedSuccess()
                val normalized = result.copy(isSuccess = success)
                val responseJson = normalized.toJson()
                _uiState.update { state ->
                    state.copy(
                        response = responseJson,
                        isSuccessful = success,
                        isUnSuccessful = !success,
                        connectionError = TransactionTransportCodes
                            .isTransientFailure(result.responseCode),
//                        status = if (success) {
//                            GetPinStatus.Waiting
//                        } else {
//                            GetPinStatus.Error
//                        },
                        // TODO:
                        errorMessage = if (success) null else result.responseMessage,
                    )
                }
                transactionCompleted = true
                if (success) {
                    Log.i(TAG, "Transaction SUCCESS | type=${result.transactionType} | code=${result.responseCode} | msg=${result.responseMessage}")
                    _events.send(GetPinEvent.TransactionSuccess(responseJson))
                } else {
                    Log.w(TAG, "Transaction FAILED | type=${result.transactionType} | code=${result.responseCode} | msg=${result.responseMessage} | transport=${TransactionTransportCodes.isTransportCode(result.responseCode)}")
                    _events.send(GetPinEvent.TransactionFailure(responseJson))
                }
            } catch (e: Exception) {
                transactionCompleted = true
                Log.e(TAG, "Transaction EXCEPTION | ${e.javaClass.simpleName}: ${e.message}", e)
                val isNetworkRelated = e is java.io.IOException
                        || e is java.net.SocketException
                        || e is java.net.ConnectException
                        || e is java.net.SocketTimeoutException
                val errorCode = if (isNetworkRelated) {
                    TransactionTransportCodes.NETWORK_ERROR
                } else {
                    "99"
                }
                val errorMessage = if (isNetworkRelated) {
                    context.getString(R.string.tx_message_network_failed)
                } else {
                    context.getString(R.string.tx_message_general_failed)
                }
                val errorResponse = TransactionResultDetail(
                    isSuccess = false,
                    responseMessage = errorMessage,
                    responseCode = errorCode,
                ).toJson()
                _uiState.update { state ->
                    state.copy(
                        status = GetPinStatus.Error,
                        errorMessage = errorMessage,
                        isUnSuccessful = true,
                        isSuccessful = false,
                        response = errorResponse,
                        connectionError = isNetworkRelated,
                    )
                }
                _events.send(GetPinEvent.TransactionFailure(errorResponse))
            } finally {
                isSubmitting = false
            }
        }
    }
}
