package com.danesh.hp.bill

import com.danesh.api.BillFlowPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpBillFlowPolicy @Inject constructor() : BillFlowPolicy {
    override val requiresAmountInput: Boolean = false
    override val requiresInquiry: Boolean = true
}
