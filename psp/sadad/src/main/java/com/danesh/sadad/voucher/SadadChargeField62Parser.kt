package com.danesh.sadad.voucher

import android.util.Log

/**
 * DE62 پاسخ CHARGE سداد.
 *
 * قالب میزبان (رشتهٔ ASCII خام، بدون hex2Str):
 * Pin_Count(n2) + Serial_Length(n2) + Pin_Length(n2) + Data(Serial سپس Pin، به تعداد Pin_Count).
 * باقیماندهٔ کوتاه بعد از خواندن کامل جفت‌ها مجاز است.
 *
 * Pin_Length طول رمز شارژ واقعی (رقم) است، ولی میزبان رمز را به‌صورت BCD با 3DES زیر کلید
 * DATA رمز می‌کند؛ پس در Data به‌جای Pin_Length کاراکتر، بلوک رمزشده به طول
 * `roundUp8(ceil(Pin_Length / 2)) * 2` کاراکتر hex می‌آید (مثلاً طول ۱۵ → ۱۶ hex).
 * در این حالت [SadadChargePins.pinEncrypted] true است و [SadadChargePins.pin] همان hex رمزشده است؛
 * رمزگشایی در [SadadChargePinCipher] انجام می‌شود.
 *
 * اگر سرآیند عددی نباشد یا طول‌ها در Data جا نشود، جداسازی فاصله / بایت `00` به‌کار می‌رود.
 */
data class SadadChargePins(
    val serial: String,
    val pin: String,
    val ussd: String = "",
    val pinCount: Int = 1,
    /** true یعنی [pin] بلوک hex رمزشده است، نه رمز شارژ قابل نمایش. */
    val pinEncrypted: Boolean = false,
    /** طول رمز شارژ اعلام‌شده در سرآیند (Pin_Length)؛ 0 اگر نامشخص. */
    val pinLength: Int = 0,
    /**
     * چیدمان‌های جایگزین متن‌ساده (رمز فقط رقم) از همان Data؛ اگر رمزگشایی [pin] رمز عددی
     * معتبر ندهد، از این‌ها استفاده می‌شود (مثلاً وقتی میزبان اول رمز و بعد سریال حرفی-عددی می‌فرستد).
     */
    val plainAlternatives: List<SadadChargePins> = emptyList(),
)

/** رمز شارژ فقط رقم است (سریال می‌تواند حروف انگلیسی هم داشته باشد). */
internal fun isValidChargePin(pin: String, pinLength: Int): Boolean =
    pin.isNotEmpty() && pin.all { it in '0'..'9' } && (pinLength <= 0 || pin.length == pinLength)

internal fun isValidChargeSerial(serial: String): Boolean =
    serial.isNotEmpty() && serial.all { it in '0'..'9' || it in 'A'..'Z' || it in 'a'..'z' }

object SadadChargeField62Parser {

    fun parse(raw: String?): SadadChargePins? {
        if (raw.isNullOrEmpty()) {
            Log.d(TAG, "4) decoded charge text= <empty raw>")
            Log.d(TAG, "5) token count=0 serial= pin= ussd=")
            return null
        }
        parseCounted(raw)?.let { return it }
        val compactHex = raw.filter { !it.isWhitespace() && it != '\u0000' }
        if (compactHex.length >= 2 && compactHex.length % 2 == 0 && compactHex.all { it.isHex() }) {
            val decoded = hex2ChargeText(compactHex)
            splitCharge(decoded)?.let { pins ->
                logDecoded(decoded, pins)
                return pins
            }
        }
        val decoded = raw.replace('\u0000', ' ')
        val pins = splitCharge(decoded)
        logDecoded(decoded, pins)
        return pins
    }

    fun parse(bytes: ByteArray?): SadadChargePins? {
        if (bytes == null || bytes.isEmpty()) return null
        return parse(String(bytes, Charsets.ISO_8859_1))
    }

    private fun parseCounted(field: String): SadadChargePins? {
        if (field.length < HEADER_LEN) return null
        val pinCount = field.substring(0, 2).toIntOrNull() ?: return null
        val serialLen = field.substring(2, 4).toIntOrNull() ?: return null
        val pinLen = field.substring(4, 6).toIntOrNull() ?: return null
        if (pinCount <= 0 || serialLen < 0 || pinLen <= 0) return null
        val data = field.substring(HEADER_LEN)
        val serial = data.take(serialLen)
        Log.d(
            TAG,
            "4) counted header pinCount=$pinCount serialLen=$serialLen pinLen=$pinLen " +
                "dataLen=${data.length}",
        )

        // چیدمان‌های متن‌ساده که رمزشان فقط رقم است: «سریال سپس رمز» (مطابق مستند) و «رمز سپس سریال».
        val plainCandidates = buildList {
            if (data.length >= serialLen + pinLen) {
                val pin = data.substring(serialLen, serialLen + pinLen)
                if (isValidChargePin(pin, pinLen) && isValidChargeSerial(serial)) {
                    add(SadadChargePins(serial = serial, pin = pin, pinCount = pinCount, pinLength = pinLen))
                }
                val pinFirst = data.take(pinLen)
                val serialAfterPin = data.substring(pinLen, pinLen + serialLen)
                if (isValidChargePin(pinFirst, pinLen) && isValidChargeSerial(serialAfterPin)) {
                    add(
                        SadadChargePins(
                            serial = serialAfterPin,
                            pin = pinFirst,
                            pinCount = pinCount,
                            pinLength = pinLen,
                        ),
                    )
                }
            }
        }
        plainCandidates.forEachIndexed { index, candidate ->
            Log.d(TAG, "5) counted PLAIN candidate#$index serial=${candidate.serial} pin=${candidate.pin}")
        }

        val cipherLen = encryptedPinHexLength(pinLen)
        val encryptedPairLen = serialLen + cipherLen
        if (data.length >= pinCount * encryptedPairLen) {
            val cipherHex = data.substring(serialLen, encryptedPairLen)
            if (cipherHex.all { it.isHex() }) {
                Log.d(
                    TAG,
                    "5) counted ENCRYPTED serial=$serial pinCipherHex=$cipherHex " +
                        "(cipherLen=$cipherLen for pinLen=$pinLen) " +
                        "remainder=${data.length - pinCount * encryptedPairLen}",
                )
                return SadadChargePins(
                    serial = serial,
                    pin = cipherHex,
                    pinCount = pinCount,
                    pinEncrypted = true,
                    pinLength = pinLen,
                    plainAlternatives = plainCandidates,
                )
            }
        }

        plainCandidates.firstOrNull()?.let { return it }

        val plainPairLen = serialLen + pinLen
        if (data.length < pinCount * plainPairLen) return null
        val pin = data.substring(serialLen, plainPairLen)
        Log.d(
            TAG,
            "5) counted PLAIN serial=$serial pin=$pin " +
                "remainder=${data.length - pinCount * plainPairLen}",
        )
        return SadadChargePins(
            serial = serial,
            pin = pin,
            pinCount = pinCount,
            pinLength = pinLen,
        )
    }

    /** طول hex بلوک 3DES رمز BCD با طول [pinLen] رقم. */
    internal fun encryptedPinHexLength(pinLen: Int): Int {
        val bcdBytes = (pinLen + 1) / 2
        val blockBytes = ((bcdBytes + 7) / 8) * 8
        return blockBytes * 2
    }

    private fun splitCharge(text: String): SadadChargePins? {
        val parts = text.split(' ').filter { it.isNotEmpty() }
        if (parts.size < 2) return null
        return SadadChargePins(
            serial = parts[0].trim(),
            pin = parts[1].trim(),
            ussd = parts.getOrNull(2).orEmpty().trim(),
        )
    }

    private fun logDecoded(decoded: String, pins: SadadChargePins?) {
        val parts = decoded.split(' ').filter { it.isNotEmpty() }
        Log.d(TAG, "4) decoded charge text=$decoded")
        Log.d(
            TAG,
            "5) token count=${parts.size} serial=${pins?.serial.orEmpty()} pin=${pins?.pin.orEmpty()} ussd=${pins?.ussd.orEmpty()}",
        )
    }

    private fun hex2ChargeText(hex: String): String {
        val bytes = ByteArray(hex.length / 2)
        for (index in bytes.indices) {
            val pair = hex.substring(index * 2, index * 2 + 2)
            val value = pair.toIntOrNull(16) ?: return ""
            bytes[index] = value.toByte()
        }
        return bytesToChargeText(bytes)
    }

    private fun bytesToChargeText(bytes: ByteArray): String {
        val text = StringBuilder(bytes.size)
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            text.append(if (value == 0) ' ' else value.toChar())
        }
        return text.toString()
    }

    private fun Char.isHex(): Boolean =
        this in '0'..'9' || this in 'A'..'F' || this in 'a'..'f'

    private const val HEADER_LEN = 6
    private const val TAG = "sharjHoma"
}
