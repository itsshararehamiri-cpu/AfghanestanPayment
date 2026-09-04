package com.danesh.common.receipt

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantNameReceiptTextTest {

    @Test
    fun truncateWithEllipsis_shortTextUnchanged() {
        assertEquals("فروشگاه", truncateWithEllipsis("فروشگاه", 20))
    }

    @Test
    fun truncateWithEllipsis_longTextEndsWithEllipsis() {
        val longName = "فروشگاه بزرگ مرکزی شهر تهران شمال"
        val result = truncateMerchantNameForReceipt(longName, isPaperReceipt = true)
        assertEquals(20, result.length)
        assertEquals("...", result.takeLast(3))
    }

    @Test
    fun truncateMerchantNameForReceipt_electronicAllowsLongerName() {
        val name = "A".repeat(25)
        assertEquals(name, truncateMerchantNameForReceipt(name, isPaperReceipt = false))
        assertEquals("A".repeat(27) + "...", truncateMerchantNameForReceipt("A".repeat(35), isPaperReceipt = false))
    }
}
