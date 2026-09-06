package com.danesh.sadad.keycard

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * موارد تست بر اساس سناریوی تست بخش ۴ و جدول کدهای خطای بخش ۵ مستند
 * «راهنمای استفاده از کارت کلید» (F-P102, v2.0.1).
 */
class SadadKeyCardApduTest {

    @Test
    fun selectApplet_cardA_matchesDocumentedCommand() {
        val expected = SadadHex.decode("00A404000B53414441444B4D53410101")
        assertArrayEquals(expected, SadadKeyCardApdu.selectApplet(SadadKeyCard.CARD_A))
    }

    @Test
    fun selectApplet_cardC_matchesDocumentedCommand() {
        val expected = SadadHex.decode("00A404000B53414441444B4D53430303")
        assertArrayEquals(expected, SadadKeyCardApdu.selectApplet(SadadKeyCard.CARD_C))
    }

    @Test
    fun verifyPin_matchesDocumentedTestVector() {
        // Command --> A8200000 04 31323334  (PIN "1234")
        val expected = SadadHex.decode("A820000004" + "31323334")
        assertArrayEquals(expected, SadadKeyCardApdu.verifyPin("1234"))
    }

    @Test
    fun readRsaPublicKey_recordOne_matchesDocumentedCommand() {
        // Command --> A8B30100
        val expected = SadadHex.decode("A8B30100")
        assertArrayEquals(expected, SadadKeyCardApdu.readRsaPublicKey(1))
    }

    @Test
    fun readRsaPrivateExponent_recordOne_matchesDocumentedCommand() {
        // Command --> A8B40100
        val expected = SadadHex.decode("A8B40100")
        assertArrayEquals(expected, SadadKeyCardApdu.readRsaPrivateExponent(1))
    }

    @Test
    fun readEncryptedKey_byteStructure_matchesDocumentedApduFormat() {
        // ساختار A8 B0 <RecordNumber P1> <KeyNumber P2> — بخش 3.4 مستند.
        // مقادیر P2 دقیقاً با سناریوی تست بخش 4.2 (A8B00100..A8B00105) یکسان است؛ نگاشت
        // نام↔شماره کلید طبق جدول بخش 3.4 است (نک: KDoc روی SadadKeyNumber).
        assertArrayEquals(
            SadadHex.decode("A8B00101"),
            SadadKeyCardApdu.readEncryptedKey(1, SadadKeyNumber.TERMINAL_MASTER_KEY),
        )
        assertArrayEquals(
            SadadHex.decode("A8B00102"),
            SadadKeyCardApdu.readEncryptedKey(1, SadadKeyNumber.MAC),
        )
        assertArrayEquals(
            SadadHex.decode("A8B00106"),
            SadadKeyCardApdu.readEncryptedKey(1, SadadKeyNumber.INIT_DATA),
        )
    }

    @Test
    fun statusWord_parsesOkResponse() {
        val response = SadadHex.decode("01039000")
        assertEquals(SadadKeyCardApdu.SW_OK, SadadKeyCardApdu.statusWord(response))
        assertArrayEquals(SadadHex.decode("0103"), SadadKeyCardApdu.body(response))
    }

    @Test
    fun verifyPinResponse_successResult_parsedFromDocumentedTestVector() {
        // Response <-- 01 03 9000
        val response = SadadHex.decode("01039000")
        val body = SadadKeyCardApdu.requireSuccess(response, "Verify PIN")
        assertTrue(body[0].toInt() == 0x01)
        assertEquals(3, body[1].toInt())
    }

    @Test
    fun requireSuccess_pukBlocked_throwsWithMappedError() {
        val response = SadadHex.decode("63A0")
        val error = assertThrowsKeyCardException { SadadKeyCardApdu.requireSuccess(response, "op") }
        assertEquals(SadadKeyCardError.PUK_BLOCKED, error.error)
        assertEquals(0x63A0, error.sw)
    }

    @Test
    fun requireSuccess_pinVerificationNeeded_throwsWithMappedError() {
        val response = SadadHex.decode("63A2")
        val error = assertThrowsKeyCardException { SadadKeyCardApdu.requireSuccess(response, "op") }
        assertEquals(SadadKeyCardError.PIN_VERIFICATION_NEEDED, error.error)
    }

    @Test
    fun requireSuccess_unknownSw_hasNullMappedError() {
        val response = SadadHex.decode("6F00")
        val error = assertThrowsKeyCardException { SadadKeyCardApdu.requireSuccess(response, "op") }
        assertEquals(null, error.error)
        assertFalse(error.message.isNullOrBlank())
    }

    private fun assertThrowsKeyCardException(block: () -> Unit): SadadKeyCardException {
        try {
            block()
        } catch (e: SadadKeyCardException) {
            return e
        }
        throw AssertionError("Expected SadadKeyCardException to be thrown")
    }
}
