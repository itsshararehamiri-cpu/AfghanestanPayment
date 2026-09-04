package com.danesh.report
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.TransactionResultDetail
import com.danesh.common.strings.AppStrings
import com.danesh.core.Device
import com.danesh.report.data.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class LastTransactionUiState(
    val result: TransactionResultDetail? = null,
    val error: String = "",
    val showLoading: Boolean = false,
    val printError: String = "",
)

@HiltViewModel
class LastTransactionViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val appStrings: AppStrings,
    private val device: Device,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LastTransactionUiState())
    val uiState: StateFlow<LastTransactionUiState> = _uiState.asStateFlow()

    fun loadLastTransaction() {
        viewModelScope.launch {
            _uiState.update { it.copy(showLoading = true) }
            val result = reportRepository.getLastTransaction()
            _uiState.update {
                it.copy(
                    result = result,
                    error = if (result == null) appStrings.noTransactionFound() else "",
                    showLoading = false,
                )
            }
        }
    }

    fun clearPrintError() {
        _uiState.update { it.copy(printError = "") }
    }

    fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        viewModelScope.launch {
            device.print(
                bitmap = bitmap,
                context = context,
                onSuccess = onSuccess,
                onFailed = { message ->
                    _uiState.update { it.copy(printError = message) }
                    onFailed(message)
                },
            )
        }
    }
}
