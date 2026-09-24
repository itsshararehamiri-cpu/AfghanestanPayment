package com.danesh.sadad.voucher

import com.danesh.sadad.keycard.SadadKeyCardCrypto
import org.jpos.iso.ISOUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class SadadChargePinCipherTest {

    private val dataKey = ISOUtil.hex2byte("0123456789ABCDEFFEDCBA9876543210")

    @Test
    fun bcdBlock_roundTripsThroughTripleDes() {
        val pin = "123456789012345"
        val block = SadadChargePinCipher.encodePinBlock(pin, 8)
        assertEquals("123456789012345F", ISOUtil.hexString(block))
        val cipher = SadadKeyCardCrypto.encrypt3DesEcb(block, dataKey)
        val plain = SadadKeyCardCrypto.decrypt3DesEcb(cipher, dataKey)
        assertEquals(pin, SadadChargePinCipher.pinFromPlainBlock(plain, 15))
    }

    @Test
    fun pinFromPlainBlock_handlesLeadingPadAsciiAndLength() {
        assertEquals(
            "123456789012345",
            SadadChargePinCipher.pinFromPlainBlock(ISOUtil.hex2byte("F123456789012345"), 15),
        )
        assertEquals(
            "12345678",
            SadadChargePinCipher.pinFromPlainBlock("12345678".toByteArray(), 8),
        )
        assertEquals(
            "1234",
            SadadChargePinCipher.pinFromPlainBlock(ISOUtil.hex2byte("1234FFFFFFFFFFFF"), 4),
        )
    }
}
