package com.danesh.iso.packager

import org.jpos.iso.IFB_BINARY
import org.jpos.iso.IFB_LLCHAR
import org.jpos.iso.IFB_LLLCHAR
import org.jpos.iso.IFB_NUMERIC
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HpIso93BPackagerTest {

    private val packager = HpIso93BPackager()

    @Test
    fun keyFields_matchHamrahPayIso93BSpec() {
        assertTrue(packager.getFieldPackager(22) is IFB_NUMERIC)
        assertEquals(12, packager.getFieldPackager(22).length)

        assertTrue(packager.getFieldPackager(35) is IFB_LLCHAR)
        assertEquals(37, packager.getFieldPackager(35).length)

        assertTrue(packager.getFieldPackager(41) is IFB_NUMERIC)
        assertEquals(8, packager.getFieldPackager(41).length)

        assertTrue(packager.getFieldPackager(42) is IFB_NUMERIC)
        assertEquals(15, packager.getFieldPackager(42).length)

        assertTrue(packager.getFieldPackager(48) is IFB_LLLCHAR)
        assertEquals(999, packager.getFieldPackager(48).length)

        assertTrue(packager.getFieldPackager(52) is IFB_BINARY)
        assertEquals(8, packager.getFieldPackager(52).length)

        assertTrue(packager.getFieldPackager(64) is IFB_BINARY)
        assertEquals(8, packager.getFieldPackager(64).length)
    }
}
