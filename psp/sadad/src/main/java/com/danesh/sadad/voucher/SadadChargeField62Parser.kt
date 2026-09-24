package com.danesh.sadad.voucher

import android.util.Log

/**
 * DE62 پاسخ CHARGE سداد.
 *
 * قالب میزبان (رشتهٔ ASCII خام، بدون hex2Str):
 * Pin_Count(n2) + Serial_Length(n2) + Pin_Length(n2) + Data(Serial سپس Pin، به تعداد Pin_Count).
 * باقیماندهٔ کوتاه بعد از خواندن کامل جفت‌ها مجاز است.
 *
 * اگر سرآیند عددی نباشد یا طول‌ها در Data جا نشود، جداسازی فاصله / بایت `00` به‌کار می‌رود.
 */
data class SadadChargePins(
    val serial: String,
    val pin: String,
    val ussd: String = "",
    val pinCount: Int = 1,
)

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
        if (pinCount <= 0 || serialLen < 0 || pinLen < 0) return null
        val pairLen = serialLen + pinLen
        if (pairLen <= 0) return null
        val data = field.substring(HEADER_LEN)
        val needed = pinCount * pairLen
        if (data.length < needed) return null
        val serial = data.substring(0, serialLen)
        val pin = data.substring(serialLen, pairLen)
        val remainderLen = data.length - needed
        Log.d(
            TAG,
            "4) counted pinCount=$pinCount serialLen=$serialLen pinLen=$pinLen " +
                "remainder=$remainderLen",
        )
        Log.d(TAG, "5) counted serial=$serial pin=$pin")
        return SadadChargePins(
            serial = serial,
            pin = pin,
            pinCount = pinCount,
        )
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
