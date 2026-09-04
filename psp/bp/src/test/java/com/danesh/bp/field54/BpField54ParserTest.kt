package com.danesh.bp.field54

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BpField54ParserTest {

    @Test
    fun parse_actualOnly() {
        val parsed = BpField54Parser.parse("364C000000050000")
        requireNotNull(parsed)
        assertEquals("364", parsed.actual.currencyCode)
        assertEquals('C', parsed.actual.debitCredit)
        assertEquals("000000050000", parsed.actual.amount)
        assertEquals("50000", parsed.actual.toDisplayAmount())
        assertNull(parsed.available)
    }

    @Test
    fun parse_actualAndAvailable() {
        val parsed = BpField54Parser.parse("364C000000050000364C000000045000")
        requireNotNull(parsed)
        assertEquals("50000", parsed.actual.toDisplayAmount())
        assertEquals("45000", parsed.available!!.toDisplayAmount())
    }

    @Test
    fun parse_debitIndicator_negativeDisplay() {
        val parsed = BpField54Parser.parse("364D000000001250")
        requireNotNull(parsed)
        assertEquals("-1250", parsed.actual.toDisplayAmount())
    }

    @Test
    fun parse_blankOrShort_returnsNull() {
        assertNull(BpField54Parser.parse(null))
        assertNull(BpField54Parser.parse(""))
        assertNull(BpField54Parser.parse("364C00000005000"))
    }

    @Test
    fun parse_invalidIndicator_returnsNull() {
        assertNull(BpField54Parser.parse("364X000000050000"))
    }
}
