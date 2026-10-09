package com.danesh.sadad.logon

import com.danesh.sadad.keycard.SadadKeyCardCrypto
import org.jpos.iso.ISOUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadSupportPasswordUpdaterTest {

    private val dataKey = ISOUtil.hex2byte("0123456789ABCDEFFEDCBA9876543210")

    @Test
    fun decryptsBcdPaddedPassword() {
        val cipher = SadadKeyCardCrypto.encrypt3DesEcb(ISOUtil.hex2byte("123456FFFFFFFFFF"), dataKey)
        assertEquals("123456", SadadSupportPasswordUpdater.decryptWith(cipher, dataKey))
    }

    @Test
    fun decryptsAsciiPassword() {
        val cipher = SadadKeyCardCrypto.encrypt3DesEcb("98765432".toByteArray(Charsets.US_ASCII), dataKey)
        assertEquals("98765432", SadadSupportPasswordUpdater.decryptWith(cipher, dataKey))
    }

    @Test
    fun wrongKeyIsRejected() {
        val cipher = SadadKeyCardCrypto.encrypt3DesEcb(ISOUtil.hex2byte("1234FFFFFFFFFFFF"), dataKey)
        val other = ISOUtil.hex2byte("11111111111111112222222222222222")
        assertTrue(runCatching { SadadSupportPasswordUpdater.decryptWith(cipher, other) }.isFailure)
    }
}
