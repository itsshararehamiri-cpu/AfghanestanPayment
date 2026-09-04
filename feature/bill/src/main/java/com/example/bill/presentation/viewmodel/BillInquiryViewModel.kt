package com.example.bill.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.BillInquiryOutput
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.strings.AppStrings
import com.example.bill.BillInquiryResultDetails
import com.example.bill.R
import com.example.bill.domain.BillInquiryUseCase
import com.example.bill.navigation.BillNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillInquiryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val details: BillInquiryResultDetails? = null,
    val billId: String = "",
    val payId: String = "",
    val amount: String = "",
    val requestId: String = "",
)

@HiltViewModel
class BillInquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billInquiryUseCase: BillInquiryUseCase,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val billId: String = savedStateHandle.get<String>(BillNavArgs.BILL_ID).orEmpty()
    private val payId: String = savedStateHandle.get<String>(BillNavArgs.PAY_ID).orEmpty()
    private val pan: String = savedStateHandle.get<String>(SwipeCardNavArgs.PAN).orEmpty()
    private val track2: String = savedStateHandle.get<String>(SwipeCardNavArgs.TRACK_2).orEmpty()

    private val _uiState = MutableStateFlow(
        BillInquiryUiState(
            billId = billId,
            payId = payId,
        ),
    )
    val uiState: StateFlow<BillInquiryUiState> = _uiState.asStateFlow()

    init {
        inquire()
    }

    fun inquire() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching {
                billInquiryUseCase(
                    billId = billId,
                    payId = payId,
                    pan = pan,
                    track2 = track2,
                )
            }.getOrElse { error ->
               val v= BillInquiryOutput(
                    isSuccess = false,
                    responseMessage = error.message ?: appStrings.reportLoadFailed(),
                    billId = billId,
                )
                Log.d("TAG", "inquire: dddddddd$v")
                Log.d("TAG", "inquire: dddddddd$error")
                Log.d("TAG", "inquire: dddddddd$v")


                v
            }
            Log.d("TAG", "inquire: dddddkkddd$result")
            if (!result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.responseMessage.ifBlank {
                            appStrings.reportLoadFailed()
                        },
                    )
                }
                return@launch
            }
            val amount = result.amount.filter { it.isDigit() }.ifBlank { "0" }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    amount = amount,
                    requestId ="",// result.requestId,
                    billId = result.billId.ifBlank { billId },
                    details = result.toUiDetails(),
                )
            }
        }
    }

    private fun BillInquiryOutput.toUiDetails(): BillInquiryResultDetails {
        val amountLabel = amount.filter { it.isDigit() }.ifBlank { "0" }
        val title =""
        /*payerName.ifBlank {
            companyCode.ifBlank { appStrings.billTypeGeneric() }
        }*/
        return BillInquiryResultDetails(
            billTypeTitle = title,
            billTypeIcon = R.drawable.ic_water_drop,
            billId = billId.ifBlank { this@BillInquiryViewModel.billId },
            debtAmount = amountLabel,
            taxAmount = "—",
            paymentDeadline = "—",
            payableAmount = amountLabel,
        )
    }
}
