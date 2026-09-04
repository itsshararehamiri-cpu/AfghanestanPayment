package com.danesh.bp.support

import org.junit.Assert.assertEquals
import org.junit.Test

class BpSupportMenuParserTest {

    @Test
    fun parsesHashStarFormat() {
        val items = BpSupportMenuParser.parse("01*کمک سیل‌زدگان*50000#02*خیریه*100000")
        assertEquals(2, items.size)
        assertEquals("01", items[0].serviceId)
        assertEquals("کمک سیل‌زدگان", items[0].title)
        assertEquals("50000", items[0].amount)
        assertEquals("02", items[1].serviceId)
        assertEquals("100000", items[1].amount)
    }

    @Test
    fun parsesPipeSemicolonFormat() {
        val items = BpSupportMenuParser.parse("A;Title A;1000|B;Title B;2000")
        assertEquals(2, items.size)
        assertEquals("A", items[0].serviceId)
        assertEquals("Title A", items[0].title)
        assertEquals("1000", items[0].amount)
        assertEquals("B", items[1].serviceId)
        assertEquals("Title B", items[1].title)
        assertEquals("2000", items[1].amount)
    }
}
