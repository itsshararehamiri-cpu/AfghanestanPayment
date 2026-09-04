package com.example.bill.domain

import com.danesh.api.BillInquiryInput
import com.danesh.api.BillInquiryOutput
import com.danesh.api.PspGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillInquiryUseCase @Inject constructor(
    private val pspGateway: PspGateway,
) {
    suspend operator fun invoke(
        billId: String,
        payId: String,
        pan: String,
        track2: String = "",
    ): BillInquiryOutput = withContext(Dispatchers.IO) {
        pspGateway.billInquiry(
            BillInquiryInput(
                billId = billId,
                payId = payId,
                pan = pan,
                track2 = track2,
            ),
        )
    }
}
