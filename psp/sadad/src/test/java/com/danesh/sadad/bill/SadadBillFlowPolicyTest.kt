package com.danesh.sadad.bill

import com.danesh.api.BillFlowPolicy
import com.danesh.api.BillInquiryKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadBillFlowPolicyTest {

    @Test
    fun skipsAmountInputAndInquiryOnPaymentPath() {
        val policy = SadadBillFlowPolicy()
        assertFalse(policy.requiresAmountInput)
        assertFalse(policy.requiresInquiry)
    }

    @Test
    fun exposesStandaloneInquiryCatalogForSadadOnly() {
        val policy = SadadBillFlowPolicy()
        assertTrue(policy.hasStandaloneInquiryTransaction)
        assertEquals(
            listOf(
                BillInquiryKind.MOBILE,
                BillInquiryKind.TELECOM,
                BillInquiryKind.WATER_ELECTRICITY,
            ),
            policy.standaloneInquiryTypes,
        )
    }

    @Test
    fun defaultPolicyHidesStandaloneInquiryForOtherPsps() {
        val policy = object : BillFlowPolicy {
            override val requiresAmountInput = true
            override val requiresInquiry = false
        }
        assertFalse(policy.hasStandaloneInquiryTransaction)
        assertTrue(policy.standaloneInquiryTypes.isEmpty())
    }
}
