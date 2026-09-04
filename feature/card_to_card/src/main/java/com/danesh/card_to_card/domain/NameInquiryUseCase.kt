package com.danesh.card_to_card.domain

import com.danesh.api.NameInquiryInput
import com.danesh.api.NameInquiryOutput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import com.danesh.card_to_card.model.TransferDestinationType
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NameInquiryUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        forWallet: Boolean,
        destination: String,
        pan: String = "",
        track2: String = "",
    ): NameInquiryOutput = withContext(Dispatchers.IO) {
        pspGateway.nameInquiry(
            NameInquiryInput(
                forWallet = forWallet,
                destination = destination.filter { it.isDigit() },
                pan = pan,
                track2 = track2,
            ),
        )
    }

    fun buildFailureDetail(
        result: NameInquiryOutput,
        destinationType: TransferDestinationType,
        destination: String,
        amount: String,
        pan: String,
    ): TransactionResultDetail {
        val transactionType = when (destinationType) {
            TransferDestinationType.WALLET -> TransactionType.CARD_TO_WALLET
            TransferDestinationType.CARD -> TransactionType.CARD_TO_CARD
        }
        val digits = destination.filter { it.isDigit() }
        val base = result.detail ?: TransactionResultDetail(
            isSuccess = false,
            transactionType = transactionType,
        )
        return base.copy(
            isSuccess = false,
            transactionType = transactionType,
            responseCode = result.responseCode.ifBlank { base.responseCode },
            responseMessage = result.responseMessage.ifBlank { base.responseMessage },
            amount = amount,
            pan = base.pan.ifBlank { pan },
            maskedPan = base.maskedPan.ifBlank { pan.maskPanForDisplay() },
            destinationPan = if (destinationType == TransferDestinationType.CARD) {
                digits.ifBlank { base.destinationPan }
            } else {
                base.destinationPan
            },
            walletCode = if (destinationType == TransferDestinationType.WALLET) {
                digits.ifBlank { base.walletCode }
            } else {
                base.walletCode
            },
            rrn = result.rrn.ifBlank { base.rrn.orEmpty() }.ifBlank { null },
        )
    }
}
