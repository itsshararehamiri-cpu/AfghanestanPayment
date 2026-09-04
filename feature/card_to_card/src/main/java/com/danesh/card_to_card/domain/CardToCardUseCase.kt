package com.danesh.card_to_card.domain

import com.danesh.api.CardToWalletInput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.card_to_card.model.TransferDestinationType
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.danesh.api.CardToCardInput as ApiCardToCardInput

class CardToCardUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,
        pan: String,
        destination: String,
        destinationType: TransferDestinationType,
        rrn: String = "",
        holderName: String = "",
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            val digits = destination.filter { it.isDigit() }
            when (destinationType) {
                TransferDestinationType.WALLET -> pspGateway.cardToWallet(
                    CardToWalletInput(
                        track2 = track2,
                        pinBlock = pinBlock,
                        amount = amountValue,
                        pan = pan,
                        walletCode = digits,
                        rrn = rrn,
                        holderName = holderName,
                    ),
                ).copy(
                    transactionType = TransactionType.CARD_TO_WALLET,
                    walletCode = digits,
                    holderName = holderName,
                )

                TransferDestinationType.CARD -> pspGateway.cardToCard(
                    ApiCardToCardInput(
                        track2 = track2,
                        pinBlock = pinBlock,
                        amount = amountValue,
                        pan = pan,
                        destinationPan = digits,
                        rrn = rrn,
                        holderName = holderName,
                    ),
                ).copy(
                    transactionType = TransactionType.CARD_TO_CARD,
                    destinationPan = digits,
                    holderName = holderName,
                )
            }
        }
    }
}
