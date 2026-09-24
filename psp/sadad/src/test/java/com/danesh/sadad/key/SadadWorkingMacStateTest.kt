package com.danesh.sadad.key

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadWorkingMacStateTest {

    @Test
    fun workingKeyIndex_isCardCPlusOne() {
        val state = SadadWorkingMacState.inMemoryForTests()
        state.saveKeyIndices(cardCIndex = 16, rsaKeyIndex = 5)

        assertEquals(16, state.keyIndex())
        assertEquals(16, state.initMacIndex())
        assertEquals(17, state.workingKeyIndex())
        assertEquals(5, state.rsaKeyIndex())
        assertEquals(16, state.persistedCardCIndex())
        assertEquals("016", state.masterKeyIndexForDe59())
        assertFalse(state.hasWorkingMac())
    }

    @Test
    fun persistedCardCIndex_isNullUntilSaved() {
        val state = SadadWorkingMacState.inMemoryForTests()
        assertNull(state.persistedCardCIndex())
        assertNull(state.rsaKeyIndex())

        state.saveRsaKeyIndex(7)
        assertEquals(7, state.rsaKeyIndex())
        assertNull(state.persistedCardCIndex())
    }

    @Test
    fun markWorkingMacLoaded_persistsFlag() {
        val state = SadadWorkingMacState.inMemoryForTests()
        state.saveKeyIndex(16)
        state.markWorkingMacLoaded()
        assertTrue(state.hasWorkingMac())
        assertEquals(17, state.workingKeyIndex())

        state.clearWorkingMac()
        assertFalse(state.hasWorkingMac())
        assertEquals(16, state.initMacIndex())
        assertEquals(17, state.workingKeyIndex())
    }
}
