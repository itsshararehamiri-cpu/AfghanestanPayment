package com.danesh.bp.key

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BpKeyMaterialTest {

    @Test
    fun initialKeyBytes_matchesConfiguredHex() {
        val expected = BpKeyConfig.INITIAL_MASTER_KEY_HEX.decodeHexKey()
        assertArrayEquals(expected, BpKeyMaterial.initialKeyBytes())
        assertEquals(24, BpKeyMaterial.initialKeyBytes().size)
    }

    private fun assertEquals(expected: Int, actual: Int) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
