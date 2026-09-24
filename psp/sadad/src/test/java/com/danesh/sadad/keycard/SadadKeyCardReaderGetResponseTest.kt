package com.danesh.sadad.keycard

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** کارت‌خوان سنترم: پاسخ طولانی با `61 xx` می‌آید و باید GET RESPONSE زده شود. */
class SadadKeyCardReaderGetResponseTest {

    private class ScriptedTransport(private val replies: ArrayDeque<ByteArray>) : SadadIccTransport {
        val sent = mutableListOf<String>()
        override fun powerOn() = true
        override fun powerOff() = Unit
        override fun isCardPresent() = true
        override suspend fun exchange(command: ByteArray): ByteArray {
            sent += SadadHex.encode(command)
            return replies.removeFirst()
        }
    }

    private fun sw(value: Int) = byteArrayOf((value shr 8).toByte(), value.toByte())

    @Test
    fun privateKey_6180_thenGetResponseReturnsFullKey() = runBlocking {
        val key = ByteArray(128) { it.toByte() }
        val transport = ScriptedTransport(ArrayDeque(listOf(sw(0x6180), key + sw(0x9000))))

        val result = SadadKeyCardReader(transport).readRsaPrivateExponent(5)

        assertArrayEquals(key, result)
        assertEquals(listOf("A8B40500", "00C0000080"), transport.sent)
    }

    @Test
    fun chained61xx_isConcatenated() = runBlocking {
        val first = ByteArray(64) { 1 }
        val second = ByteArray(64) { 2 }
        val transport = ScriptedTransport(
            ArrayDeque(listOf(sw(0x6140), first + sw(0x6140), second + sw(0x9000))),
        )

        val result = SadadKeyCardReader(transport).readRsaPublicModulus(5)

        assertArrayEquals(first + second, result)
        assertEquals(listOf("A8B30500", "00C0000040", "00C0000040"), transport.sent)
    }

    @Test
    fun wrongLength6Cxx_resendsWithLe() = runBlocking {
        val key = ByteArray(128) { 7 }
        val transport = ScriptedTransport(ArrayDeque(listOf(sw(0x6C80), key + sw(0x9000))))

        val result = SadadKeyCardReader(transport).readRsaPublicModulus(5)

        assertArrayEquals(key, result)
        assertEquals(listOf("A8B30500", "A8B3050080"), transport.sent)
    }
}
