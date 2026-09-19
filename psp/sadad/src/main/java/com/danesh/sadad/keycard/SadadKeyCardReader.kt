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

    /** کلید عمومی RSA (128 بایت Modulus) از کارت A. */
    suspend fun readRsaPublicModulus(recordNumber: Int): ByteArray {
        val b = SadadKeyCardApdu.readRsaPublicKey(recordNumber)
        val response = transport.exchange(b)
        val response2 = transport.exchange(ISOUtil.hex2byte("00C0000080"))

        Log.d(
            "TAG",
            "SadadKeyCardReaderreadRsaPublicModuluschhhom: ${ISOUtil.hexString(response2)}"
        )

        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModulus: $recordNumber")
        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModuluscom: ${ISOUtil.hexString(b)}")

        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModulus: ${ISOUtil.hexString(response)}")
        val temp = SadadKeyCardApdu.requireSuccess(response2, "Read RSA public key")
        Log.d("TAG", "SadadKeyCardReaderreadRsaPublicModulus: ${ISOUtil.hexString(temp)}")

        return temp
    }

    /** کلید خصوصی RSA (128 بایت Private Exponent) از کارت A. */
    suspend fun readRsaPrivateExponent(recordNumber: Int): ByteArray {
        val response = transport.exchange(SadadKeyCardApdu.readRsaPrivateExponent(recordNumber))
        Log.d("TAG", "SadadKeyCardReaderreadRsaPrivateExponent: $recordNumber")
        Log.d("TAG", "SadadKeyCardReaderreadRsaPrivateExponent: ${ISOUtil.hexString(response)}")
        val response2 = transport.exchange(ISOUtil.hex2byte("00C0000080"))
        val temp = SadadKeyCardApdu.requireSuccess(response2, "Read RSA private key")
        Log.d("TAG", "SadadKeyCardReaderreadRsaPrivateExponent: ${ISOUtil.hexString(temp)}")
        return temp
    }

    /** کلید (128 بایت رمزشده با RSA) از کارت B یا C. */
    suspend fun readEncryptedKey(recordNumber: Int, keyNumber: SadadKeyNumber): ByteArray {
        Log.d("TAG", "SadadKeyCardReaderreadEnjjjcryptedKey: $recordNumber")
        Log.d("TAG", "SadadKeyCardReaderreadEncryptjkjkedKey: $keyNumber")
        val t = SadadKeyCardApdu.readEncryptedKey(recordNumber, keyNumber)
        val response = transport.exchange(t)
        Log.d("TAG", "SadadKeyCardReaderreadEncryptedKkjkkey: ${ISOUtil.hexString(t)}")

        Log.d("TAG", "SadadKeyCardReaderreadEncryptedKkjkkey: ${ISOUtil.hexString(response)}")
        val response2 = transport.exchange(ISOUtil.hex2byte("00C0000080"))

        val temp = SadadKeyCardApdu.requireSuccess(response2, "Read encrypted ${keyNumber.name}")
        Log.d("TAG", "SadadKeyCardReaderreadEncryptedKey: ${ISOUtil.hexString(temp)}")
        Log.d("TAG", "SadadKeyCardReaderreadEncryptedKey: ${ISOUtil.hexString(response2)}")

        return temp
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
