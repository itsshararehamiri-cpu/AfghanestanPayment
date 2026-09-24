package com.danesh.sadad.keycard

import android.util.Log
import org.jpos.iso.ISOUtil
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
        Log.d("TAG", "selectAppletSadadKeyCardReader: ${ISOUtil.hexString(response)}")
        val temp = SadadKeyCardApdu.requireSuccess(response, "Select applet ${card.name}")
        Log.d("TAG", "selectApplet SadadKeyCardReader: ${ISOUtil.hexString(temp)}")

    }

    suspend fun verifyPin(pin: String): SadadPinVerificationResult {
        val verifyPinCommand = SadadKeyCardApdu.verifyPin(pin)
        Log.d("TAG", "verifyPin: ${ISOUtil.hexString(verifyPinCommand)}")
        val response = transport.exchange(verifyPinCommand)
        val body = SadadKeyCardApdu.requireSuccess(response, "Verify PIN")
        require(body.size >= 2) { "Unexpected Verify PIN response length: ${body.size}" }
        Log.d("TAG", "verifyPin SadadKeyCardReader: ${ISOUtil.hexString(response)}")

        val temp = SadadPinVerificationResult(
            success = body[0].toInt() == 0x01,
            remainingTries = body[1].toInt() and 0xFF,
        )
        Log.d("TAG", "verifyPin SadadKeyCardReader: ${temp}")
        return temp
    }

    private suspend fun exchangeUntilComplete(command: ByteArray, operation: String): ByteArray {
        var response = transport.exchange(command)
        SadadKeyCardApdu.moreDataLength(SadadKeyCardApdu.statusWord(response))?.let { le ->
            response = transport.exchange(SadadKeyCardApdu.getResponse(le))
        }
        return SadadKeyCardApdu.requireSuccess(response, operation)
    }

    /** کلید عمومی RSA (128 بایت Modulus) از کارت A. */
    suspend fun readRsaPublicModulus(recordNumber: Int): ByteArray {
        val b = SadadKeyCardApdu.readRsaPublicKey(recordNumber)
        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModulus: $recordNumber")
        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModuluscom: ${ISOUtil.hexString(b)}")
        return exchangeUntilComplete(b, "Read RSA public key")
    }

    /** کلید خصوصی RSA (128 بایت Private Exponent) از کارت A. */
    suspend fun readRsaPrivateExponent(recordNumber: Int): ByteArray {
        val command = SadadKeyCardApdu.readRsaPrivateExponent(recordNumber)
        Log.d("TAG", "SadadKeyCardReaderreadRsaPrivateExponent: $recordNumber")
        return exchangeUntilComplete(command, "Read RSA private key")
    }

    /** کلید (128 بایت رمزشده با RSA) از کارت B یا C. */
    suspend fun readEncryptedKey(recordNumber: Int, keyNumber: SadadKeyNumber): ByteArray {
        val command = SadadKeyCardApdu.readEncryptedKey(recordNumber, keyNumber)
        Log.d("TAG", "SadadKeyCardReaderreadEncryptedKey: $recordNumber $keyNumber")
        return exchangeUntilComplete(command, "Read encrypted ${keyNumber.name}")
    }

    fun powerOn(): Boolean {
        Log.d("TAG", "SadadKeyCardReaderpowerOn: ")
        return transport.powerOn()
    }

    fun powerOff() {
        Log.d("TAG", "SadadKeyCardReaderpowerOff: ")
        return transport.powerOff()
    }
}
