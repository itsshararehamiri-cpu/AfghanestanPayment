package com.danesh.wallet_to_wallet.domain

import com.danesh.api.NameInquiryInput
import com.danesh.api.NameInquiryOutput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NameInquiryUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        sourceWallet: String,
        destinationWallet: String,
    ): NameInquiryOutput = withContext(Dispatchers.IO) {
        pspGateway.nameInquiry(
            NameInquiryInput(
                forWallet = false,
                forWalletToWallet = true,
                destination = destinationWallet.filter { it.isDigit() },
                sourceWallet = sourceWallet.filter { it.isDigit() },
            ),
        )
    }

    fun buildFailureDetail(
        result: NameInquiryOutput,
        sourceWallet: String,
        destinationWallet: String,
        amount: String,
    ): TransactionResultDetail {
        val sourceDigits = sourceWallet.filter { it.isDigit() }
        val destDigits = destinationWallet.filter { it.isDigit() }
        val base = result.detail ?: TransactionResultDetail(
            isSuccess = false,
            transactionType = TransactionType.WALLET_TO_WALLET,
        )
        return base.copy(
            isSuccess = false,
            transactionType = TransactionType.WALLET_TO_WALLET,
            responseCode = result.responseCode.ifBlank { base.responseCode },
            responseMessage = result.responseMessage.ifBlank { base.responseMessage },
            amount = amount,
            pan = base.pan.ifBlank { sourceDigits },
            maskedPan = base.maskedPan.ifBlank { sourceDigits.maskPanForDisplay() },
            destinationPan = destDigits.ifBlank { base.destinationPan },
            rrn = result.rrn.ifBlank { base.rrn.orEmpty() }.ifBlank { null },
        )
    }
}
