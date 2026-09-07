package com.danesh.card_to_card.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.danesh.api.TransferFlowPolicy
import com.danesh.card_to_card.model.CardToCardInput
import com.danesh.card_to_card.model.TransferDestinationType
import com.danesh.card_to_card.ui.cardNumberForSubmit
import com.danesh.card_to_card.ui.normalizeDigits
import com.danesh.common.strings.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CardToCardTransferUiState(
    val destinationType: TransferDestinationType = TransferDestinationType.CARD,
    val supportsWallet: Boolean = false,
    val destination: String = "",
    val amountText: String = "",
    val amountValue: Int = 0,
    val destinationError: String? = null,
    val amountError: String? = null,
)

@HiltViewModel
class CardToCardTransferViewModel @Inject constructor(
    private val appStrings: AppStrings,
    transferFlowPolicy: TransferFlowPolicy,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CardToCardTransferUiState(supportsWallet = transferFlowPolicy.supportsWalletTransfer),
    )
    val uiState: StateFlow<CardToCardTransferUiState> = _uiState.asStateFlow()

    fun onDestinationTypeChange(type: TransferDestinationType) {
        _uiState.update {
            it.copy(
                destinationType = type,
                destination = "",
                destinationError = null,
            )
        }
    }

    fun onDestinationChange(value: String) {
        val maxLen = if (_uiState.value.destinationType == TransferDestinationType.WALLET) 16 else 16
        _uiState.update {
            it.copy(
                destination = normalizeDigits(value).take(maxLen),
                destinationError = null,
            )
        }
    }

    fun onAmountChange(input: String) {
        val digits = normalizeDigits(input)
        _uiState.update {
            it.copy(
                amountValue = digits.toIntOrNull() ?: 0,
                amountText = digits,
                amountError = null,
            )
        }
    }

    fun validateAndProceed(onSuccess: (CardToCardInput) -> Unit) {
        val state = _uiState.value
        val normalized = cardNumberForSubmit(state.destination)
        val isWallet = state.destinationType == TransferDestinationType.WALLET

        var destinationError: String? = null
        var amountError: String? = null

        if (normalized.isEmpty()) {
            destinationError = if (isWallet) {
                appStrings.validationEnterDestinationCard()
            } else {
                appStrings.validationEnterDestinationCard()
            }
        } else if (!isWallet && normalized.length != 16) {
            destinationError = appStrings.validationCard16Digits()
        } else if (isWallet && normalized.length != 16) {
            destinationError = appStrings.validationWallet8Digits()
        }

        if (state.amountValue <= 0) {
            amountError = appStrings.validationEnterAmount()
        }

        if (destinationError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    destinationError = destinationError,
                    amountError = amountError,
                )
            }
            return
        }

        onSuccess(
            CardToCardInput(
                destination = normalized,
                amount = state.amountValue,
                destinationType = state.destinationType,
            ),
        )
    }
}
