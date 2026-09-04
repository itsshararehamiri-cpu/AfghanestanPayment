package com.example.bill
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
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
class UnSuccessBillViewModel @Inject constructor(
    private val device: Device,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResultTransactionUiState())
    val uiState: StateFlow<ResultTransactionUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(response: String) {
        viewModelScope.launch {
            val result = Gson().fromJson(response, TransactionResultDetail::class.java)
            Log.d("TAG", "initss: hhhhhh$result")
            _uiState.update { it.copy(result = result) }
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorInPrint = "") }
        }
    }

    fun setErrorInPrint(errorInPrint: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorInPrint = errorInPrint) }
        }
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

data class ResultTransactionUiState(
    val result: TransactionResultDetail? = null,
    val error: String = "",
    val autoPrintCustomerReceipt: Boolean = false,
    val customerReceiptPrinted: Boolean = false,
    val merchantReceiptPrinted: Boolean = false,
    val responseForCallerApp: String? = null,
    val playbackSound: Boolean = false,
    val errorInPrint: String = "",
    val showPrintForMerchant: Boolean = false,
    val showPrintForCustomer: Boolean = false,
)
