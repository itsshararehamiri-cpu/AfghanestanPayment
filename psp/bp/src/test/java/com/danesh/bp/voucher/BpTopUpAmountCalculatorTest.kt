package com.danesh.bp.voucher

import org.junit.Assert.assertEquals
import org.junit.Test

class BpTopUpAmountCalculatorTest {

    @Test
    fun totalWithVat_addsVatToChargeAmount() {
        assertEquals(
            109_000L,
            BpTopUpAmountCalculator.totalWithVat(chargeAmount = 100_000L, vatPercentRaw = "9"),
        )
    }

    @Test
    fun totalWithVat_returnsChargeWhenVatMissing() {
        assertEquals(
            50_000L,
            BpTopUpAmountCalculator.totalWithVat(chargeAmount = 50_000L, vatPercentRaw = ""),
        )
    }

    @Test
    fun formatIsoAmount_padsToTwelveDigits() {
        assertEquals("000000010000", BpTopUpAmountCalculator.formatIsoAmount(10_000L))
    }
}
