package com.danesh.bp.logon

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BpLogonField62ParserTest {

    @Test
    fun field62_splitsWireOrderIntoMacPinDataBlocks() {
        val field62 = ByteArray(48) { index -> index.toByte() }
        val keys = BpLogonField62Parser.parse(field62)

        assertEquals(16, keys.encryptedMacKey.size)
        assertEquals(16, keys.encryptedPinKey.size)
        assertEquals(16, keys.encryptedDataKey.size)

        assertArrayEquals(
            field62.copyOfRange(0, 16),
            keys.encryptedMacKey,
        )
        assertArrayEquals(
            field62.copyOfRange(16, 32),
            keys.encryptedPinKey,
        )
        assertArrayEquals(
            field62.copyOfRange(32, 48),
            keys.encryptedDataKey,
        )
    }
}
