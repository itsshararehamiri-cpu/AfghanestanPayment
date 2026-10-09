package com.danesh.coupon.presentation

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.api.parseTransactionResultDetail
import com.danesh.common.receipt.QueueCustomerReceiptPrintTracker
import com.danesh.core.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CouponResultUiState(
    val result: TransactionResultDetail? = null,
    val printError: String = "",
)

/** نتیجهٔ خرید کالابرگ (موفق/ناموفق) و چاپ رسید. */
@HiltViewModel
class CouponResultViewModel @Inject constructor(
    private val device: Device,
    private val queuePrintTracker: QueueCustomerReceiptPrintTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouponResultUiState())
    val uiState: StateFlow<CouponResultUiState> = _uiState.asStateFlow()

    fun init(response: String) {
        if (_uiState.value.result != null) return
        _uiState.update { it.copy(result = parseTransactionResultDetail(response)) }
    }

    fun markCustomerReceiptForQueue() {
        queuePrintTracker.markHandled(viewModelScope, _uiState.value.result)
    }

    fun print(bitmap: Bitmap, context: Context, onSuccess: () -> Unit, onFailed: (String) -> Unit) {
        viewModelScope.launch { device.print(bitmap, context, onSuccess, onFailed) }
    }

    fun onPrintFailed(message: String) = _uiState.update { it.copy(printError = message) }

    fun clearPrintError() = _uiState.update { it.copy(printError = "") }
}
