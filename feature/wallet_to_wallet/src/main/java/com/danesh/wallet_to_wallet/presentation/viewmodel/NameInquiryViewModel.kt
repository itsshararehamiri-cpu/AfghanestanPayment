package com.danesh.wallet_to_wallet.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.strings.AppStrings
import com.danesh.wallet_to_wallet.domain.NameInquiryUseCase
import com.danesh.wallet_to_wallet.navigation.WalletToWalletNavArgs
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
    val sourceWallet: String = "",
    val destinationWallet: String = "",
    val amount: String = "",
    val rrn: String = "",
)

@HiltViewModel
class NameInquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nameInquiryUseCase: NameInquiryUseCase,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val sourceWallet: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.SOURCE_WALLET).orEmpty()
    private val destinationWallet: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.DESTINATION_WALLET).orEmpty()
    private val amount: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.AMOUNT).orEmpty()

    private val _uiState = MutableStateFlow(
        NameInquiryUiState(
            sourceWallet = sourceWallet,
            destinationWallet = destinationWallet,
            amount = amount,
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
            val result = runCatching {
                nameInquiryUseCase(
                    sourceWallet = sourceWallet,
                    destinationWallet = destinationWallet,
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
                    sourceWallet = sourceWallet,
                    destinationWallet = destinationWallet,
                    amount = amount,
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
