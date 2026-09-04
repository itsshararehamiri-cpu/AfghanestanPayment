package com.danesh.api

import org.junit.Assert.assertEquals
import org.junit.Test

class PanMaskingTest {

    @Test
    fun maskPanForDisplay_masksSixPlusFourFormat() {
        assertEquals("603799******1234", "6037991234561234".maskPanForDisplay())
    }

    @Test
    fun maskPanForDisplay_preservesAlreadyMaskedValue() {
        assertEquals("603799******1234", "603799******1234".maskPanForDisplay())
    }

    @Test
    fun maskPanForDisplay_returnsEmptyForBlankInput() {
        assertEquals("", "   ".maskPanForDisplay())
    }

    @Test
    fun maskPanForDisplay_returnsShortPanUnchanged() {
        assertEquals("123456789", "123456789".maskPanForDisplay())
    }
}
