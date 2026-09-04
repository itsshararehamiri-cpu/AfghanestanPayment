package com.danesh.cashout

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.api.parseTransactionResultDetail
import com.danesh.common.receipt.QueueCustomerReceiptPrintTracker
import com.danesh.core.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuccessCashOutViewModel @Inject constructor(
    private val device: Device,
    private val queuePrintTracker: QueueCustomerReceiptPrintTracker,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CashOutResultUiState())
    val uiState: StateFlow<CashOutResultUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(response: String) {
        viewModelScope.launch {
            val result = parseTransactionResultDetail(response) ?: return@launch
            _uiState.update { it.copy(result = result) }
        }
    }

    fun markCustomerReceiptForQueue() {
        queuePrintTracker.markHandled(viewModelScope, uiState.value.result)
    }

    fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            device.print(bitmap, context, onSuccess, onFailed)
        }
    }

}
