package com.danesh.common.receipt

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.QueueCustomerPrintMarker
import com.danesh.api.SafQueueFlusher
import com.danesh.api.SafQueueReader
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionResultDetail
import com.danesh.api.toTransactionResultDetail
import com.danesh.core.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PendingSafReceiptViewModel @Inject constructor(
    private val safQueueReader: SafQueueReader,
    private val contextProvider: TransactionContextProvider,
    private val printMarker: QueueCustomerPrintMarker,
    private val safQueueFlusher: SafQueueFlusher,
    private val device: Device,
    private val safQueueSettlementService: SafQueueSettlementService,
) : ViewModel() {

    private val _pendingReceipt = MutableStateFlow<TransactionResultDetail?>(null)
    val pendingReceipt: StateFlow<TransactionResultDetail?> = _pendingReceipt.asStateFlow()

    init {
        viewModelScope.launch {
            safQueueSettlementService.refreshPendingReceipt.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val item = safQueueReader.peekFirstPending()
            _pendingReceipt.update {
                if (item != null && !item.customerReceiptPrinted) {
                    item.toTransactionResultDetail(contextProvider.getTerminalConfig())
                } else {
                    null
                }
            }
        }
    }

    fun onCustomerReceiptHandled() {
        val detail = _pendingReceipt.value ?: return
        if (detail.date.isBlank() || detail.time.isBlank()) return
        viewModelScope.launch {
            printMarker.markCustomerReceiptPrinted(detail.date, detail.time)
            safQueueFlusher.flushAfterCustomerReceipt()
            refresh()
        }
    }

    fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            device.print(bitmap, context, onSuccess, onFailed)
        }
    }
}
