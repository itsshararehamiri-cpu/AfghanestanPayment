package com.danesh.support.presentation




import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionFeeCalculator
import com.danesh.api.TransactionResultDetail
import com.danesh.common.merchant.MerchantDisplayPreferences
import com.danesh.common.receipt.QueueCustomerReceiptPrintTracker
import com.danesh.core.Device
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SuccessSupportViewModel @Inject constructor(
    private val device: Device,
    private val queuePrintTracker: QueueCustomerReceiptPrintTracker,
    private val transactionFeeCalculator: TransactionFeeCalculator,
    private val merchantDisplayPreferences: MerchantDisplayPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BalanceSuccessResultUiState())
    val uiState: StateFlow<BalanceSuccessResultUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(response: String) {
        viewModelScope.launch {
            val result = Gson().fromJson(response, TransactionResultDetail::class.java)
            val showFee = merchantDisplayPreferences.isShowFeeEnabled()
            val feeAmount = if (showFee) {
                transactionFeeCalculator.feeForTransaction(result).toString()
            } else {
                ""
            }
            _uiState.update {
                it.copy(
                    result = result,
                    showFeeEnabled = showFee,
                    feeAmount = feeAmount,
                )
            }
        }
    }

    fun markCustomerReceiptForQueue() {

        queuePrintTracker.markHandled(viewModelScope, uiState.value.result)
    }

    fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            device.print(bitmap, context, onSuccess, onFailed)
        }
    }
}

data class BalanceSuccessResultUiState(
    val result: TransactionResultDetail? = null,
    val showFeeEnabled: Boolean = false,
    val feeAmount: String = "",
    val error: String = "",
    val playbackSound: Boolean = false
)
