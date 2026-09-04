package com.danesh.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.strings.AppStrings
import com.danesh.report.data.ReportRepository
import com.danesh.report.model.NumberedTransaction
import com.danesh.report.model.toNumberedTransactions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LastTenSuccessfulTransactionsUiState(
    val transactions: List<NumberedTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
)

@HiltViewModel
class LastTenSuccessfulTransactionsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LastTenSuccessfulTransactionsUiState())
    val uiState: StateFlow<LastTenSuccessfulTransactionsUiState> = _uiState.asStateFlow()

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            try {
                val transactions = reportRepository
                    .getLastSuccessfulTransactions(limit = MAX_SUCCESSFUL_TURNOVER_COUNT)
                    .toNumberedTransactions()
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        isLoading = false,
                        error = "",
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        transactions = emptyList(),
                        isLoading = false,
                        error = appStrings.reportLoadFailed(),
                    )
                }
            }
        }
    }

    companion object {
        const val MAX_SUCCESSFUL_TURNOVER_COUNT = 10
    }
}
