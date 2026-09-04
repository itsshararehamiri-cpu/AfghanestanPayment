package com.danesh.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VatPercentageRulesTest {

    @Test
    fun `resolve uses default when stored value is blank`() {
        assertEquals("10", VatPercentageRules.resolve(null))
        assertEquals("10", VatPercentageRules.resolve(""))
        assertEquals("10", VatPercentageRules.resolve("   "))
    }

    @Test
    fun `resolve keeps stored value`() {
        assertEquals("9", VatPercentageRules.resolve("9"))
    }

    @Test
    fun `normalize integer percent`() {
        assertEquals("10", VatPercentageRules.normalize("10"))
        assertEquals("1", VatPercentageRules.normalize("1"))
        assertEquals("100", VatPercentageRules.normalize("100"))
    }

    @Test
    fun `detect vat configuration support item`() {
        assertTrue(
            VatPercentageRules.isVatConfigurationItem("درصد مالیات ارزش افزوده"),
        )
        assertFalse(
            VatPercentageRules.isVatConfigurationItem("کمک سیل‌زدگان"),
        )
    }

    @Test
    fun `validate accepts values from 1 to 100`() {
        assertNull(VatPercentageRules.validate("1"))
        assertNull(VatPercentageRules.validate("10"))
        assertNull(VatPercentageRules.validate("100"))
        assertEquals(
            VatPercentageValidationError.OUT_OF_RANGE,
            VatPercentageRules.validate("0"),
        )
        assertEquals(
            VatPercentageValidationError.OUT_OF_RANGE,
            VatPercentageRules.validate("101"),
        )
        assertEquals(
            VatPercentageValidationError.INVALID,
            VatPercentageRules.validate("9.5"),
        )
    }
}
