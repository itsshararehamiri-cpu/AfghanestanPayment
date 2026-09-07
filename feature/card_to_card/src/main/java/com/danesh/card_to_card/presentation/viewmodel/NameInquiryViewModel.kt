package com.danesh.card_to_card.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.card_to_card.domain.NameInquiryUseCase
import com.danesh.card_to_card.model.TransferDestinationType
import com.danesh.card_to_card.navigation.CardToCardNavArgs
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.strings.AppStrings
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NameInquiryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val failureResponseJson: String? = null,
    val holderName: String = "",
    val destination: String = "",
    val amount: String = "",
    val destinationType: TransferDestinationType = TransferDestinationType.CARD,
    val rrn: String = "",
)

@HiltViewModel
class NameInquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nameInquiryUseCase: NameInquiryUseCase,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val destination: String =
        savedStateHandle.get<String>(CardToCardNavArgs.DESTINATION).orEmpty()
    private val amount: String =
        savedStateHandle.get<String>(CardToCardNavArgs.AMOUNT).orEmpty()
    private val destinationType: TransferDestinationType =
        TransferDestinationType.fromNav(
            savedStateHandle.get<String>(CardToCardNavArgs.DEST_TYPE).orEmpty(),
        )
    private val pan: String = savedStateHandle.get<String>(SwipeCardNavArgs.PAN).orEmpty()
    private val track2: String = savedStateHandle.get<String>(SwipeCardNavArgs.TRACK_2).orEmpty()

    private val _uiState = MutableStateFlow(
        NameInquiryUiState(
            destination = destination,
            amount = amount,
            destinationType = destinationType,
        ),
    )
    val uiState: StateFlow<NameInquiryUiState> = _uiState.asStateFlow()

    init {
        inquire()
    }

    fun inquire() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    failureResponseJson = null,
                )
            }
            Log.d("TAG", "CardToCardNavHost: ddtrdddddddddddddj$destinationType")

            val result = runCatching {
                nameInquiryUseCase(
                    forWallet = destinationType == TransferDestinationType.WALLET,
                    destination = destination,
                    pan = pan,
                    track2 = track2,
                )
            }.getOrElse { error ->
                com.danesh.api.NameInquiryOutput(
                    isSuccess = false,
                    responseMessage = error.message ?: appStrings.reportLoadFailed(),
                )
            }
            if (!result.isSuccess || result.holderName.isBlank()) {
                val failureDetail = nameInquiryUseCase.buildFailureDetail(
                    result = result,
                    destinationType = destinationType,
                    destination = destination,
                    amount = amount,
                    pan = pan,
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = failureDetail.responseMessage.ifBlank {
                            appStrings.reportLoadFailed()
                        },
                        failureResponseJson = Gson().toJson(failureDetail),
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    failureResponseJson = null,
                    holderName = result.holderName,
                    rrn = result.rrn,
                )
            }
        }
    }
}
