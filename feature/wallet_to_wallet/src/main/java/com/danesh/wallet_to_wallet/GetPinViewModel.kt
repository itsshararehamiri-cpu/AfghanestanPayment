package com.danesh.wallet_to_wallet

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.SwipeCardNavArgs
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import com.danesh.wallet_to_wallet.domain.WalletToWalletUseCase
import com.danesh.wallet_to_wallet.navigation.WalletToWalletNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val walletToWalletUseCase: WalletToWalletUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.AMOUNT).orEmpty()
    private val sourceWallet: String =
        savedStateHandle.get<String>(SwipeCardNavArgs.PAN).orEmpty()
    private val destinationWallet: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.DESTINATION_WALLET).orEmpty()
    private val rrn: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.RRN).orEmpty()
    private val holderName: String =
        savedStateHandle.get<String>(WalletToWalletNavArgs.RECIPIENT_NAME).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        return walletToWalletUseCase(
            pinBlock = pinBlock,
            amount = amount,
            sourceWallet = sourceWallet.ifBlank { pan },
            destinationWallet = destinationWallet,
            rrn = rrn,
            holderName = holderName,
        )
    }
}
