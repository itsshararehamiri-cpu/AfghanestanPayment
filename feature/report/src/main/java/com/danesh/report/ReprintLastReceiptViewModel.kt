package com.danesh.report

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.common.receipt.pansMatchForReprint
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

enum class ReprintLastReceiptPhase {
    Loading,
    AwaitingCard,
    WrongCard,
    ReadyToPrint,
    Printing,
    NotFound,
    PrintFailed,
    Complete,
}

data class ReprintLastReceiptUiState(
    val phase: ReprintLastReceiptPhase = ReprintLastReceiptPhase.Loading,
    val transaction: TransactionResultDetail? = null,
    val wrongCardMessage: String = "",
    val errorMessage: String = "",
)

@HiltViewModel
class ReprintLastReceiptViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val device: Device,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReprintLastReceiptUiState())
    val uiState: StateFlow<ReprintLastReceiptUiState> = _uiState.asStateFlow()

    fun start(notFoundMessage: String) {
        viewModelScope.launch {
            _uiState.value = ReprintLastReceiptUiState(phase = ReprintLastReceiptPhase.Loading)
            val transaction = reportRepository.getLastTransactionForReprint()
            if (transaction == null) {
                _uiState.update {
                    it.copy(
                        phase = ReprintLastReceiptPhase.NotFound,
                        errorMessage = notFoundMessage,
                    )
                }
                return@launch
            }
            val withReportTime = transaction.copy(reprintReportDateTime = currentReportRetrievalTime())
            if (transaction.transactionType == TransactionType.VOUCHER) {
                _uiState.update {
                    it.copy(
                        phase = ReprintLastReceiptPhase.AwaitingCard,
                        transaction = withReportTime,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        phase = ReprintLastReceiptPhase.ReadyToPrint,
                        transaction = withReportTime,
                    )
                }
            }
        }
    }

    fun onCardRead(pan: String, wrongCardMessage: String) {
        val transaction = _uiState.value.transaction ?: return
        if (!pansMatchForReprint(pan, transaction.maskedPan)) {
            _uiState.update {
                it.copy(
                    phase = ReprintLastReceiptPhase.WrongCard,
                    wrongCardMessage = wrongCardMessage,
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                phase = ReprintLastReceiptPhase.ReadyToPrint,
                wrongCardMessage = "",
            )
        }
    }

    fun retryCardSwipe() {
        _uiState.update {
            it.copy(
                phase = ReprintLastReceiptPhase.AwaitingCard,
                wrongCardMessage = "",
            )
        }
    }

    fun printWithBitmap(
        context: Context,
        bitmap: Bitmap,
        printFailedMessage: String,
    ) {
        if (_uiState.value.phase == ReprintLastReceiptPhase.Printing) return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ReprintLastReceiptPhase.Printing) }
            val printed = runCatching { printBitmap(context, bitmap) }
            _uiState.update {
                it.copy(
                    phase = if (printed.isSuccess) {
                        ReprintLastReceiptPhase.Complete
                    } else {
                        ReprintLastReceiptPhase.PrintFailed
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
        _uiState.value = ReprintLastReceiptUiState()
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
                    reportErrorToUi = false,
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
