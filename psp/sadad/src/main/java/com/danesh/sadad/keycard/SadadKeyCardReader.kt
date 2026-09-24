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
        val temp = exchangeUntilComplete(
            SadadKeyCardApdu.selectApplet(card),
            "Select applet ${card.name}",
        )
        Log.d("TAG", "selectApplet SadadKeyCardReader: ${ISOUtil.hexString(temp)}")

    }

    suspend fun verifyPin(pin: String): SadadPinVerificationResult {
        val verifyPinCommand = SadadKeyCardApdu.verifyPin(pin)
        Log.d("TAG", "verifyPin: ${ISOUtil.hexString(verifyPinCommand)}")
        val body = exchangeUntilComplete(verifyPinCommand, "Verify PIN")
        require(body.size >= 2) { "Unexpected Verify PIN response length: ${body.size}" }

        val temp = SadadPinVerificationResult(
            success = body[0].toInt() == 0x01,
            remainingTries = body[1].toInt() and 0xFF,
        )
        Log.d("TAG", "verifyPin SadadKeyCardReader: ${temp}")
        return temp
    }

    /**
     * کارت‌خوان سنترم (T=0) پاسخ طولانی را خودش جمع نمی‌کند:
     * - `61 xx`: کار موفق بوده و xx بایت دیگر آماده است ← باید `00 C0 00 00 xx` (GET RESPONSE) زده شود؛
     *   اگر باز `61 yy` آمد، تکرار و داده‌ها پشت سر هم جمع می‌شوند.
     * - `6C xx`: طول درخواستی اشتباه بوده ← همان دستور با Le = xx دوباره فرستاده می‌شود.
     * خروجی: بدنه پاسخ کامل (بدون SW) در صورت 9000، وگرنه [SadadKeyCardException].
     */
    private suspend fun exchangeUntilComplete(command: ByteArray, operation: String): ByteArray {
        var response = transport.exchange(command)
        Log.d(APDU_TAG, "$operation -> ${ISOUtil.hexString(response)}")
        SadadKeyCardApdu.wrongLengthLe(SadadKeyCardApdu.statusWord(response))?.let { le ->
            response = transport.exchange(SadadKeyCardApdu.withLe(command, le))
            Log.d(APDU_TAG, "$operation (Le=$le) -> ${ISOUtil.hexString(response)}")
        }
        val collected = java.io.ByteArrayOutputStream()
        var rounds = 0
        while (true) {
            val more = SadadKeyCardApdu.moreDataLength(SadadKeyCardApdu.statusWord(response)) ?: break
            check(rounds++ < MAX_GET_RESPONSE_ROUNDS) { "$operation: too many GET RESPONSE rounds" }
            collected.write(SadadKeyCardApdu.body(response))
            val getResponse = SadadKeyCardApdu.getResponse(more)
            response = transport.exchange(getResponse)
            Log.d(
                APDU_TAG,
                "$operation GET RESPONSE ${ISOUtil.hexString(getResponse)} -> ${ISOUtil.hexString(response)}",
            )
        }
        val last = SadadKeyCardApdu.requireSuccess(response, operation)
        collected.write(last)
        return collected.toByteArray()
    }

    private companion object {
        const val APDU_TAG = "SadadIccApdu"
        const val MAX_GET_RESPONSE_ROUNDS = 16
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
