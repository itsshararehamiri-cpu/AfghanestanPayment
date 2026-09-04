package com.danesh.report

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.core.Device
import com.danesh.report.data.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

enum class UnsettledTransactionPhase {
    Loading,
    NotFound,
    ReadyToPrint,
    Printing,
    PrintFailed,
    Complete,
}

data class UnsettledTransactionUiState(
    val phase: UnsettledTransactionPhase = UnsettledTransactionPhase.Loading,
    val transactions: List<TransactionResultDetail> = emptyList(),
    val currentIndex: Int = 0,
    val errorMessage: String = "",
) {
    val currentTransaction: TransactionResultDetail?
        get() = transactions.getOrNull(currentIndex)
}

@HiltViewModel
class UnsettledTransactionViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val device: Device,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnsettledTransactionUiState())
    val uiState: StateFlow<UnsettledTransactionUiState> = _uiState.asStateFlow()

    fun start(notFoundMessage: String) {
        viewModelScope.launch {
            _uiState.value = UnsettledTransactionUiState(phase = UnsettledTransactionPhase.Loading)
            val transactions = reportRepository.getUnsettledTransactions()
            if (transactions.isEmpty()) {
                _uiState.update {
                    it.copy(
                        phase = UnsettledTransactionPhase.NotFound,
                        errorMessage = notFoundMessage,
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    phase = UnsettledTransactionPhase.ReadyToPrint,
                    transactions = transactions,
                    currentIndex = 0,
                )
            }
        }
    }

    fun printWithBitmap(
        context: Context,
        bitmap: Bitmap,
        printFailedMessage: String,
    ) {
        if (_uiState.value.phase == UnsettledTransactionPhase.Printing) return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = UnsettledTransactionPhase.Printing) }
            val printed = runCatching { printBitmap(context, bitmap) }
            if (printed.isFailure) {
                _uiState.update {
                    it.copy(
                        phase = UnsettledTransactionPhase.PrintFailed,
                        errorMessage = printed.exceptionOrNull()?.message?.takeIf { msg ->
                            msg.isNotBlank()
                        } ?: printFailedMessage,
                    )
                }
                return@launch
            }
            val nextIndex = _uiState.value.currentIndex + 1
            if (nextIndex >= _uiState.value.transactions.size) {
                _uiState.update {
                    it.copy(phase = UnsettledTransactionPhase.Complete)
                }
            } else {
                _uiState.update {
                    it.copy(
                        phase = UnsettledTransactionPhase.ReadyToPrint,
                        currentIndex = nextIndex,
                    )
                }
            }
        }
    }

    fun reset() {
        _uiState.value = UnsettledTransactionUiState()
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
}
