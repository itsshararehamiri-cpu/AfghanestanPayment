package com.danesh.sadad.field54

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SadadField54ParserTest {

    @Test
    fun parse_asciiTwoItems_ledgerAndAvailable() {
        val parsed = SadadField54Parser.parse(
            "0101364C0000004476040102364D000000437604",
        )
        requireNotNull(parsed)
        assertEquals("447604", parsed.actual)
        assertEquals("437604", parsed.available)
    }

    @Test
    fun parse_hexAsciiDump() {
        val parsed = SadadField54Parser.parse(
            "30313031333634433030303030303434373630343031303233363444303030303030343337363034",
        )
        requireNotNull(parsed)
        assertEquals("447604", parsed.actual)
        assertEquals("437604", parsed.available)
    }

    @Test
    fun parse_shortOrBlank_returnsNull() {
        assertNull(SadadField54Parser.parse(null))
        assertNull(SadadField54Parser.parse(""))
        assertNull(SadadField54Parser.parse("0101364C"))
    }
}
