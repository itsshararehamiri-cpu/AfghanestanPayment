package com.danesh.topup.domain
import com.danesh.api.PspGateway
import com.danesh.api.TopUpInput
import com.danesh.api.TransactionResultDetail
import com.danesh.api.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
class TopUpUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        pinBlock: String,
        track2: String,
        amount: String,
        pan: String,
        mobileNumber: String,
        operatorCode: String,
    ): TransactionResultDetail {
        return withContext(Dispatchers.IO) {
            val amountValue = amount.replace(",", "").toLongOrNull() ?: 0L
            pspGateway.topUp(
                TopUpInput(
                    track2 = track2,
                    pinBlock = pinBlock,
                    amount = amountValue,
                    pan = pan,
                    mobileNumber = mobileNumber,
                    operatorCode = operatorCode,
                ),
            ).copy(transactionType = TransactionType.VOUCHER)
        }
    }
}
