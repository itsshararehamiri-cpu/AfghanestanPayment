package com.danesh.sadad.bill

import com.danesh.api.BillFlowPolicy
import com.danesh.api.BillInquiryKind
import com.danesh.api.BillPaymentValidation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadBillFlowPolicy @Inject constructor() : BillFlowPolicy {
    override fun validateBillPayment(billId: String, paymentId: String): BillPaymentValidation =
        SadadBillIdValidator.validate(billId, paymentId)

    override val requiresAmountInput: Boolean = false
    override val requiresInquiry: Boolean = false
    override val hasStandaloneInquiryTransaction: Boolean = true
    override val standaloneInquiryTypes: List<BillInquiryKind> = listOf(
        BillInquiryKind.MOBILE,
        BillInquiryKind.TELECOM,
        BillInquiryKind.WATER_ELECTRICITY,
    )
}
