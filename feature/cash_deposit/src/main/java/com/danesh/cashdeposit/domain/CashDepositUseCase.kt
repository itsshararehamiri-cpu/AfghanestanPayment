package com.danesh.cashdeposit.domain

import com.danesh.api.CashDepositInput
import com.danesh.api.CashDepositOutput
import com.danesh.api.PspGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CashDepositUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,pan: String
    ): CashDepositOutput {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.cashDeposit(
                CashDepositInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue, destinationAccount = "6037998166102202",pan=pan
                ),
            )
        }
    }
}
