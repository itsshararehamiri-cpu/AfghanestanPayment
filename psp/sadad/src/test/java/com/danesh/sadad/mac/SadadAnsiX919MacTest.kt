package com.danesh.sadad.mac

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SadadAnsiX919MacTest {

    private val key = hex("0123456789ABCDEFFEDCBA9876543210")

    /** پیام پرداخت قبض واقعی از لاگ (۱۵۲ بایت، مضرب ۸). */
    private val billMacInput = hex(
        "02003020058020C11021170000000001898000003008021F007F14386037998166102201D0909101113400" +
            "0000002F333430393139303330303030303031333136373835333000263630333936323833303132323630" +
            "3030303138393834303833350" +
            "4A80A5E4457793400443330303030303030313630313032303330343035303631334431563238393030303030303130313630303034",
    )

    @Test
    fun alignedInputGetsNoExtraPaddingBlock() {
        assertEquals(152, billMacInput.size)
        assertEquals(0, billMacInput.size % 8)
        // مرجع مستقل: DES-CBC(K1) + 3DES-EDE(K1,K2,K1) روی بلوک آخر
        assertArrayEquals(hex("3D8D6C7111ACD6B8"), SadadAnsiX919Mac.calculate(key, billMacInput))
    }

    @Test
    fun extraZeroBlockChangesTheMac() {
        val withExtraBlock = SadadAnsiX919Mac.calculate(key, billMacInput + ByteArray(8))
        assertArrayEquals(hex("3EC24C19D78E6E8A"), withExtraBlock)
        assertFalse(withExtraBlock.contentEquals(SadadAnsiX919Mac.calculate(key, billMacInput)))
    }

    @Test
    fun unalignedInputIsZeroPadded() {
        val probe = hex("53414441444D41")
        assertArrayEquals(hex("CFB94A7C1F7540D1"), SadadAnsiX919Mac.calculate(key, probe))
        assertArrayEquals(
            SadadAnsiX919Mac.calculate(key, probe + 0.toByte()),
            SadadAnsiX919Mac.calculate(key, probe),
        )
    }

    private fun hex(value: String): ByteArray =
        value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
