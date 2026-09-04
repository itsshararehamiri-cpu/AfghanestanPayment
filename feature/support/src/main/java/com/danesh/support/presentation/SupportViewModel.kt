package com.danesh.support.presentation

import android.util.Log
import com.danesh.api.SupportCatalog
import com.danesh.api.SupportMenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SupportUiState(
    val items: List<SupportMenuItem> = emptyList(),
    val selected: SupportMenuItem? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportCatalog: SupportCatalog,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        refreshItems()
    }

    fun refreshItems() {
        val  v=supportCatalog.items()
        Log.d("TAG", "refreshItems:${v.size} ")
        v.forEach {
            Log.d("TAG", "refreshItems:${it} ")

        }
        _uiState.update {
            it.copy(
                items = supportCatalog.items(),
                selected = null,
                errorMessage = null,
            )
        }
    }

    fun onItemSelected(item: SupportMenuItem) {
        _uiState.update { it.copy(selected = item, errorMessage = null) }
    }

    fun validateAndProceed(
        selectItemError: String,
        invalidAmountError: String,
        onConfirm: (serviceId: String, amount: String, title: String) -> Unit,
    ) {
        val selected = _uiState.value.selected
        if (selected == null) {
            _uiState.update { it.copy(errorMessage = selectItemError) }
            return
        }
        val amount = selected.amount.filter(Char::isDigit)
        if (amount.isEmpty() || amount.toLongOrNull() == 0L) {
            _uiState.update { it.copy(errorMessage = invalidAmountError) }
            return
        }
        onConfirm(selected.serviceId, amount, selected.title)
    }
}
