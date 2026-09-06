package com.danesh.sadad.keycard

import javax.inject.Inject
import javax.inject.Singleton

data class SadadPinVerificationResult(
    val success: Boolean,
    val remainingTries: Int,
)

/**
 * پیاده‌سازی روال کلیدگذاری بخش 3 مستند: انتخاب اپلت، تایید PIN و خواندن رکوردهای کلید.
 * این کلاس فقط تبادل APDU خام را انجام می‌دهد؛ رمزگشایی و تزریق کلید در
 * [SadadKeyCardService] و [SadadKeyCardInjector] انجام می‌شود.
 */
@Singleton
class SadadKeyCardReader @Inject constructor(
    private val transport: SadadIccTransport,
) {

    fun isCardPresent(): Boolean = transport.isCardPresent()

    suspend fun selectApplet(card: SadadKeyCard) {
        val response = transport.exchange(SadadKeyCardApdu.selectApplet(card))
        SadadKeyCardApdu.requireSuccess(response, "Select applet ${card.name}")
    }

    suspend fun verifyPin(pin: String): SadadPinVerificationResult {
        val response = transport.exchange(SadadKeyCardApdu.verifyPin(pin))
        val body = SadadKeyCardApdu.requireSuccess(response, "Verify PIN")
        require(body.size >= 2) { "Unexpected Verify PIN response length: ${body.size}" }
        return SadadPinVerificationResult(
            success = body[0].toInt() == 0x01,
            remainingTries = body[1].toInt() and 0xFF,
        )
    }

    /** کلید عمومی RSA (128 بایت Modulus) از کارت A. */
    suspend fun readRsaPublicModulus(recordNumber: Int): ByteArray {
        val response = transport.exchange(SadadKeyCardApdu.readRsaPublicKey(recordNumber))
        return SadadKeyCardApdu.requireSuccess(response, "Read RSA public key")
    }

    /** کلید خصوصی RSA (128 بایت Private Exponent) از کارت A. */
    suspend fun readRsaPrivateExponent(recordNumber: Int): ByteArray {
        val response = transport.exchange(SadadKeyCardApdu.readRsaPrivateExponent(recordNumber))
        return SadadKeyCardApdu.requireSuccess(response, "Read RSA private key")
    }

    /** کلید (128 بایت رمزشده با RSA) از کارت B یا C. */
    suspend fun readEncryptedKey(recordNumber: Int, keyNumber: SadadKeyNumber): ByteArray {
        val response = transport.exchange(SadadKeyCardApdu.readEncryptedKey(recordNumber, keyNumber))
        return SadadKeyCardApdu.requireSuccess(response, "Read encrypted ${keyNumber.name}")
    }

    fun powerOn(): Boolean = transport.powerOn()

    fun powerOff() = transport.powerOff()
}
