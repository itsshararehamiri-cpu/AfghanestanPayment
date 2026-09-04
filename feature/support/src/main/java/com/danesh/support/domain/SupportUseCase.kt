package com.danesh.support.domain

import com.danesh.api.PspGateway
import com.danesh.api.SupportInput
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SupportUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,
        pan: String,
        serviceId: String,
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.support(
                SupportInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue,
                    pan = pan,
                    serviceId = serviceId,
                ),
            ).copy(transactionType = TransactionType.SUPPORT)
        }
    }
}
