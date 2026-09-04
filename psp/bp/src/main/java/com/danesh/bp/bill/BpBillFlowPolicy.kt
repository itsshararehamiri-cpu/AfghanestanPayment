package com.danesh.bp.bill

import com.danesh.api.BillFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpBillFlowPolicy @Inject constructor() : BillFlowPolicy {
    override val requiresAmountInput: Boolean = true
    override val requiresInquiry: Boolean = false
}
