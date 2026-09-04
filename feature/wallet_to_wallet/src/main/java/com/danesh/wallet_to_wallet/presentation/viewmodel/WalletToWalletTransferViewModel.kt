package com.danesh.wallet_to_wallet.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.danesh.wallet_to_wallet.R
import com.danesh.wallet_to_wallet.model.WalletToWalletInput
import com.danesh.wallet_to_wallet.ui.normalizeDigits
import com.danesh.wallet_to_wallet.ui.walletNumberForSubmit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WalletToWalletTransferUiState(
    val sourceWallet: String = "",
    val destinationWallet: String = "",
    val amountText: String = "",
    val amountValue: Int = 0,
    val sourceWalletError: String? = null,
    val destinationWalletError: String? = null,
    val amountError: String? = null,
)

@HiltViewModel
class WalletToWalletTransferViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletToWalletTransferUiState())
    val uiState: StateFlow<WalletToWalletTransferUiState> = _uiState.asStateFlow()

    fun onSourceWalletChange(value: String) {
        _uiState.update {
            it.copy(
                sourceWallet = normalizeDigits(value).take(16),
                sourceWalletError = null,
            )
        }
    }

    fun onDestinationWalletChange(value: String) {
        _uiState.update {
            it.copy(
                destinationWallet = normalizeDigits(value).take(16),
                destinationWalletError = null,
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

    fun validateAndProceed(onSuccess: (WalletToWalletInput) -> Unit) {
        val state = _uiState.value
        val source = walletNumberForSubmit(state.sourceWallet)
        val destination = walletNumberForSubmit(state.destinationWallet)

        var sourceWalletError: String? = null
        var destinationWalletError: String? = null
        var amountError: String? = null

        if (source.isEmpty()) {
            sourceWalletError = context.getString(R.string.wallet_to_wallet_validation_enter_source)
        } else if (source.length != 16) {
            sourceWalletError = context.getString(R.string.wallet_to_wallet_validation_16_digits)
        }

        if (destination.isEmpty()) {
            destinationWalletError = context.getString(R.string.wallet_to_wallet_validation_enter_dest)
        } else if (destination.length != 16) {
            destinationWalletError = context.getString(R.string.wallet_to_wallet_validation_16_digits)
        } else if (source.isNotEmpty() && source == destination) {
            destinationWalletError = context.getString(R.string.wallet_to_wallet_validation_same_wallet)
        }

        if (state.amountValue <= 0) {
            amountError = context.getString(R.string.wallet_to_wallet_validation_enter_amount)
        }

        if (sourceWalletError != null || destinationWalletError != null || amountError != null) {
            _uiState.update {
                it.copy(
                    sourceWalletError = sourceWalletError,
                    destinationWalletError = destinationWalletError,
                    amountError = amountError,
                )
            }
            return
        }

        onSuccess(
            WalletToWalletInput(
                sourceWallet = source,
                destinationWallet = destination,
                amount = state.amountValue,
            ),
        )
    }
}
