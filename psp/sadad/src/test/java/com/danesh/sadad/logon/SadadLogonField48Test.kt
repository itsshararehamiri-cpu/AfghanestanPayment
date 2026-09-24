package com.danesh.sadad.logon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadLogonField48Test {

    @Test
    fun changeKeyZero_restIsAbsent() {
        val parsed = SadadLogonField48Parser.parse("00")!!
        assertFalse(parsed.tmsNeed)
        assertFalse(parsed.changeKeyNeed)
        assertEquals("", parsed.pinKey)
        assertEquals("", parsed.macKey)
        assertEquals("", parsed.dataKey)
        assertFalse(SadadLogonField48Parser.hasFullKeys(parsed))
    }

    @Test
    fun tmsNeedWithoutKeys() {
        val parsed = SadadLogonField48Parser.parse("10")!!
        assertTrue(parsed.tmsNeed)
        assertFalse(parsed.changeKeyNeed)
        assertEquals("", parsed.pinKey)
    }

    @Test
    fun changeKeyNeed_parsesThreeDesKeys() {
        val pin = "A".repeat(32)
        val mac = "B".repeat(32)
        val data = "C".repeat(32)
        val parsed = SadadLogonField48Parser.parse("01$pin$mac$data")!!
        assertFalse(parsed.tmsNeed)
        assertTrue(parsed.changeKeyNeed)
        assertEquals(pin, parsed.pinKey)
        assertEquals(mac, parsed.macKey)
        assertEquals(data, parsed.dataKey)
        assertTrue(SadadLogonField48Parser.hasFullKeys(parsed))
    }
}
