package com.danesh.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OptionalReceiptRulesTest {

    private val limits = OptionalReceiptLimits(active = true, lowerRials = 100_000, upperRials = 5_000_000)

    @Test
    fun belowLowerNoCustomerReceipt() {
        assertEquals(CustomerReceiptMode.NONE, OptionalReceiptRules.customerReceiptMode(limits, 99_999))
    }

    @Test
    fun betweenLimitsAsks() {
        assertEquals(CustomerReceiptMode.ASK, OptionalReceiptRules.customerReceiptMode(limits, 100_000))
        assertEquals(CustomerReceiptMode.ASK, OptionalReceiptRules.customerReceiptMode(limits, 5_000_000))
    }

    @Test
    fun aboveUpperMandatory() {
        assertEquals(CustomerReceiptMode.MANDATORY, OptionalReceiptRules.customerReceiptMode(limits, 5_000_001))
    }

    @Test
    fun inactiveOrMissingAlwaysMandatory() {
        assertEquals(CustomerReceiptMode.MANDATORY, OptionalReceiptRules.customerReceiptMode(null, 1))
        assertEquals(
            CustomerReceiptMode.MANDATORY,
            OptionalReceiptRules.customerReceiptMode(limits.copy(active = false), 1),
        )
    }

    @Test
    fun validation() {
        assertEquals(OptionalReceiptValidationError.EMPTY, OptionalReceiptRules.validate("", "1"))
        assertEquals(OptionalReceiptValidationError.LOWER_GREATER_THAN_UPPER, OptionalReceiptRules.validate("10", "5"))
        assertNull(OptionalReceiptRules.validate("5", "10"))
    }
}
