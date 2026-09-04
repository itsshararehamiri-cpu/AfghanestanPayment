package com.danesh.common.receipt

import org.junit.Assert.assertEquals
import org.junit.Test

class StanRrnDisplayFormatTest {

    @Test
    fun formatStanRrnDisplay_withBoth_returnsCombined() {
        assertEquals("123456/789012345678", formatStanRrnDisplay("123456", "789012345678"))
    }

    @Test
    fun formatStanRrnDisplay_withoutRrn_returnsStanOnly() {
        assertEquals("123456", formatStanRrnDisplay("123456", null))
        assertEquals("123456", formatStanRrnDisplay("123456", ""))
        assertEquals("123456", formatStanRrnDisplay("123456", "   "))
    }

    @Test
    fun formatStanRrnDisplay_withoutStan_returnsRrnOnly() {
        assertEquals("789012345678", formatStanRrnDisplay("", "789012345678"))
    }

    @Test
    fun formatStanRrnDisplay_withoutBoth_returnsEmpty() {
        assertEquals("", formatStanRrnDisplay("", null))
    }
}
