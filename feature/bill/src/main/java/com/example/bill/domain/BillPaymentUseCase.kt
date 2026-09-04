package com.example.bill.domain

import com.danesh.api.BillInput
import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillPaymentUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,
        pan: String,
        billId: String,
        payId: String,
        requestId: String = "",
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val normalizedAmount = amount.replace(",", "").ifBlank { "0" }
            pspGateway.bill(
                BillInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    billId = billId,
                    payId = payId,
                    pan = pan,
                    amount = normalizedAmount,
                    requestId = requestId,
                ),
            ).copy(transactionType = TransactionType.BILL)
        }
    }
}
