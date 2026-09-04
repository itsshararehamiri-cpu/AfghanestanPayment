package com.danesh.cashout.domain

import com.danesh.api.CashOutInput
import com.danesh.api.CashOutOutput
import com.danesh.api.PspGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CashOutUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,pan: String
    ): CashOutOutput {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.cashOut(
                CashOutInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue, destinationAccount = "6037998166102202",
                    pan=pan
                ),
            )
        }
    }
}
