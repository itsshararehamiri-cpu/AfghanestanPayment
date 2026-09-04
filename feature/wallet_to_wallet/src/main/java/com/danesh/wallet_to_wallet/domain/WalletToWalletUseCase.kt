package com.danesh.wallet_to_wallet.domain

import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.WalletToWalletInput
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletToWalletUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        amount: String,
        sourceWallet: String,
        destinationWallet: String,
        rrn: String = "",
        holderName: String = "",
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            val sourceDigits = sourceWallet.filter { it.isDigit() }
            val destDigits = destinationWallet.filter { it.isDigit() }
            pspGateway.walletToWallet(
                WalletToWalletInput(
                    pinBlock = pinBlock,
                    amount = amountValue,
                    sourceWallet = sourceDigits,
                    destinationWallet = destDigits,
                    rrn = rrn,
                    holderName = holderName,
                ),
            ).copy(
                transactionType = TransactionType.WALLET_TO_WALLET,
                pan = sourceDigits,
                destinationPan = destDigits,
                holderName = holderName,
            )
        }
    }
}
