package com.danesh.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.report.data.ReportRepository
import com.danesh.report.model.ReportFilterState
import com.danesh.report.model.ReportMenuType
import com.danesh.report.model.ReportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val summary: ReportSummary = ReportSummary("0", "0"),
    val isLoading: Boolean = true,
)

data class ReportFlowState(
    val reportType: ReportMenuType? = null,
    val filters: ReportFilterState = ReportFilterState(),
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _flowState = MutableStateFlow(ReportFlowState())
    val flowState: StateFlow<ReportFlowState> = _flowState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val summary = reportRepository.getSummary()
            _uiState.update { it.copy(summary = summary, isLoading = false) }
        }
    }

    fun setReportType(type: ReportMenuType) {
        _flowState.update { it.copy(reportType = type) }
    }

    fun setFilters(filters: ReportFilterState) {
        _flowState.update { it.copy(filters = filters) }
    }
}
