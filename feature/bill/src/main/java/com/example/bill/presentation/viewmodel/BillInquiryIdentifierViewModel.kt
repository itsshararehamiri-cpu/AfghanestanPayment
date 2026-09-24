package com.example.bill.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.danesh.api.BillInquiryKind
import com.danesh.common.strings.AppStrings
import com.example.bill.navigation.BillNavArgs
import com.example.bill.parseBillInquiryKind
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BillInquiryIdentifierUiState(
    val kind: BillInquiryKind = BillInquiryKind.WATER_ELECTRICITY,
    val identifier: String = "",
    val identifierError: String? = null,
)

@HiltViewModel
class BillInquiryIdentifierViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val kind: BillInquiryKind =
        parseBillInquiryKind(savedStateHandle.get<String>(BillNavArgs.BILL_TYPE).orEmpty())
            ?: BillInquiryKind.WATER_ELECTRICITY

    private val _uiState = MutableStateFlow(BillInquiryIdentifierUiState(kind = kind))
    val uiState: StateFlow<BillInquiryIdentifierUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value.filter(Char::isDigit), identifierError = null) }
    }

    fun validateAndProceed(onSuccess: (billId: String, payId: String) -> Unit) {
        val identifier = _uiState.value.identifier.trim()
        val error = validationError(identifier)
        if (error != null) {
            _uiState.update { it.copy(identifierError = error) }
            return
        }
        // Assumption: Sadad inquiry DE48 is only Bill_ID(13)+Payment_ID(13). A type that
        // collects one identifier (mobile / line / bill id) is sent as Bill_ID; Payment_ID
        // is zeros until the 0110 response supplies the real pair.
        onSuccess(identifier, "")
    }

    private fun validationError(identifier: String): String? {
        if (identifier.isEmpty()) {
            return when (kind) {
                BillInquiryKind.MOBILE -> appStrings.validationEnterMobile()
                BillInquiryKind.TELECOM -> appStrings.get(com.example.bill.R.string.bill_inquiry_enter_line)
                BillInquiryKind.WATER_ELECTRICITY -> appStrings.validationEnterBillId()
            }
        }
        if (kind == BillInquiryKind.MOBILE && identifier.length !in 10..11) {
            return appStrings.validationMobile10Digits()
        }
        return null
    }
}
