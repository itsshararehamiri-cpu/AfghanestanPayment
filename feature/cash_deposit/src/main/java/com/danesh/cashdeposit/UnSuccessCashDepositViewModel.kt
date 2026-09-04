package com.danesh.cashdeposit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.api.parseTransactionResultDetail
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
class UnSuccessCashDepositViewModel @Inject constructor(
    private val device: Device,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CashDepositResultUiState())
    val uiState: StateFlow<CashDepositResultUiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(response: String) {
        viewModelScope.launch {
            val result = parseTransactionResultDetail(response) ?: return@launch
            _uiState.update { it.copy(result = result) }
        }
    }

    fun setErrorInPrint(errorInPrint: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorInPrint = errorInPrint) }
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorInPrint = "") }
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

data class CashDepositResultUiState(
    val result: TransactionResultDetail? = null,
    val errorInPrint: String = "",
)
