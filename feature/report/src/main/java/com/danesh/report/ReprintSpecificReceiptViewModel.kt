package com.danesh.report

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.core.Device
import com.danesh.report.data.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class ReprintSpecificReceiptPhase {
    Input,
    Loading,
    ReadyToPrint,
    Printing,
    NotFound,
    PrintFailed,
    Complete,
}

data class ReprintSpecificReceiptUiState(
    val phase: ReprintSpecificReceiptPhase = ReprintSpecificReceiptPhase.Input,
    val trackingNumber: String = "",
    val referenceNumber: String = "",
    val transaction: TransactionResultDetail? = null,
    val errorMessage: String = "",
)

@HiltViewModel
class ReprintSpecificReceiptViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val device: Device,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReprintSpecificReceiptUiState())
    val uiState: StateFlow<ReprintSpecificReceiptUiState> = _uiState.asStateFlow()

    fun updateTrackingNumber(value: String) {
        _uiState.update { it.copy(trackingNumber = value, errorMessage = "") }
    }

    fun updateReferenceNumber(value: String) {
        _uiState.update { it.copy(referenceNumber = value, errorMessage = "") }
    }

    fun submit(notFoundMessage: String) {
        val tracking = _uiState.value.trackingNumber.trim()
        val reference = _uiState.value.referenceNumber.trim()
        if (tracking.isEmpty() && reference.isEmpty()) {
            _uiState.update {
                it.copy(
                    phase = ReprintSpecificReceiptPhase.NotFound,
                    errorMessage = notFoundMessage,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ReprintSpecificReceiptPhase.Loading, errorMessage = "") }
            val transaction = reportRepository.getTransactionForReprint(
                trackingNumber = tracking,
                referenceNumber = reference,
            )
            if (transaction == null) {
                _uiState.update {
                    it.copy(
                        phase = ReprintSpecificReceiptPhase.NotFound,
                        errorMessage = notFoundMessage,
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    phase = ReprintSpecificReceiptPhase.ReadyToPrint,
                    transaction = transaction.copy(reprintReportDateTime = currentReportRetrievalTime()),
                )
            }
        }
    }

    fun dismissNotFound() {
        _uiState.update {
            it.copy(
                phase = ReprintSpecificReceiptPhase.Input,
                errorMessage = "",
            )
        }
    }

    fun printWithBitmap(
        context: Context,
        bitmap: Bitmap,
        printFailedMessage: String,
    ) {
        if (_uiState.value.phase == ReprintSpecificReceiptPhase.Printing) return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ReprintSpecificReceiptPhase.Printing) }
            val printed = runCatching { printBitmap(context, bitmap) }
            _uiState.update {
                it.copy(
                    phase = if (printed.isSuccess) {
                        ReprintSpecificReceiptPhase.Complete
                    } else {
                        ReprintSpecificReceiptPhase.PrintFailed
                    },
                    errorMessage = if (printed.isSuccess) {
                        ""
                    } else {
                        printed.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                            ?: printFailedMessage
                    },
                )
            }
        }
    }

    fun reset() {
        _uiState.value = ReprintSpecificReceiptUiState()
    }

    private suspend fun printBitmap(context: Context, bitmap: Bitmap) {
        suspendCancellableCoroutine { continuation ->
            val job = CoroutineScope(continuation.context).launch {
                device.print(
                    bitmap = bitmap,
                    context = context,
                    onSuccess = {
                        if (continuation.isActive) continuation.resume(Unit)
                    },
                    onFailed = { message ->
                        if (continuation.isActive) {
                            continuation.resumeWith(
                                Result.failure(IllegalStateException(message)),
                            )
                        }
                    },
                )
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }

    private fun currentReportRetrievalTime(): String {
        val now = Date()
        val date = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(now)
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(now)
        return "$date $time"
    }
}
