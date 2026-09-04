package com.danesh.purchase.domain

import com.danesh.api.PspGateway
import com.danesh.api.PurchaseInput
import com.danesh.api.PurchaseOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PurchaseUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,pan: String
    ): PurchaseOutput {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.purchase(
                PurchaseInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue,pan=pan
                ),
            )
        }
    }
}
