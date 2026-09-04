package com.danesh.sadad.bill

import com.danesh.api.BillFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadBillFlowPolicy @Inject constructor() : BillFlowPolicy {
    override val requiresAmountInput: Boolean = false
    override val requiresInquiry: Boolean = true
}
