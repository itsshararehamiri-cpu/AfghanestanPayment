package com.danesh.sadad.charge

import com.danesh.api.ChargeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadChargeListParserTest {

    @Test
    fun parsesEnabledVoucherAndTopUpFromChargeList() {
        val snapshot = javaClass.classLoader!!
            .getResourceAsStream("sadad_charge_list.xml")
            .use { SadadChargeListParser.parse(it!!) }

        assertEquals(listOf("919", "935", "921"), snapshot.voucherOperators.map { it.providerId })
        assertEquals(listOf("919", "935", "921"), snapshot.topUpOperators.map { it.providerId })

        val mciVoucher = snapshot.products.filter {
            it.kind == ChargeKind.VOUCHER && it.providerId == "919"
        }
        assertEquals(listOf(200_000L, 500_000L, 1_000_000L), mciVoucher.map { it.amountRials })
        assertEquals("5", mciVoucher.first().categoryId)
        assertEquals("*140*#Pin#", mciVoucher.first().loadUssd)

        val irancellTopUp = snapshot.products.filter {
            it.kind == ChargeKind.TOPUP && it.providerId == "935"
        }
        assertEquals(2, irancellTopUp.size)
        assertTrue(irancellTopUp.all { it.amountRials == null })
        assertEquals("01", irancellTopUp[0].serviceTypeCode)
        assertEquals("02", irancellTopUp[1].serviceTypeCode)
        assertEquals(9, snapshot.topUpOperators.first { it.providerId == "935" }.taxPercent)
        assertEquals(220_000L, snapshot.topUpOperators.first { it.providerId == "935" }.minChargeAmount)

        assertNull(snapshot.products.firstOrNull { it.providerId == "932" })
    }
}
