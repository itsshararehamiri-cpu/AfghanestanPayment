package com.danesh.voucher.domain

import com.danesh.api.PspGateway
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import com.danesh.api.VoucherInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VoucherUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,
        pan: String,
        operatorCode: String,
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.voucher(
                VoucherInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue,
                    pan = pan,
                    operatorCode = operatorCode,
                ),
            ).copy(transactionType = TransactionType.VOUCHER)
        }
    }
}
