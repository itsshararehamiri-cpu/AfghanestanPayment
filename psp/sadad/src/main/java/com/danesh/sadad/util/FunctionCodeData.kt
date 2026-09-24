package com.danesh.sadad.util

import java.nio.charset.Charset

/**
 * بدنهٔ Private4 (DE63) سداد:
 *
 * `n2` Count، سپس برای هر آیتم: `n3` Function Code + `n3` Data Length + `ans` Data.
 *
 * جدول کلی سند طول را فقط به‌صورت `ans …` نشان می‌دهد؛ نمونه‌های خود سند
 * (`01040000`، `0104800201`) طول سه‌رقمی را قبل از داده دارند.
 * طول‌ها روی بایت‌های همان charset فیلد CHAR (ISO-8859-1) شمرده می‌شوند، نه code pointهای UTF-16.
 */
data class FunctionCodeData(
    val code: String,
    val data: String,
)

object SadadField63Wire {
    val CHARSET: Charset = Charsets.ISO_8859_1
}

object Field63Generator {

    fun generate(functionCodes: List<FunctionCodeData>): String {
        require(functionCodes.size <= 99) {
            "Function code count cannot be more than 99"
        }
        val out = StringBuilder()
        out.append(functionCodes.size.toString().padStart(2, '0'))
        for (item in functionCodes) {
            val code = item.code.trim().padStart(3, '0')
            require(code.length == 3 && code.all { it.isDigit() }) {
                "Function code must be exactly 3 digits"
            }
            val dataBytes = item.data.toByteArray(SadadField63Wire.CHARSET)
            require(dataBytes.size <= 999) {
                "Function data length cannot be more than 999"
            }
            out.append(code)
            out.append(dataBytes.size.toString().padStart(3, '0'))
            out.append(String(dataBytes, SadadField63Wire.CHARSET))
        }
        return out.toString()
    }
}

object Field63Parser {

    fun parse(field63: String): List<FunctionCodeData> {
        if (field63.isBlank()) return emptyList()
        return parse(field63.toByteArray(SadadField63Wire.CHARSET))
    }

    fun parse(raw: ByteArray): List<FunctionCodeData> {
        if (raw.isEmpty()) return emptyList()
        return runCatching { parseWithDataLength(raw) }
            .getOrElse { parseSingleWithoutDataLength(raw) }
    }

    private fun parseWithDataLength(raw: ByteArray): List<FunctionCodeData> {
        val cursor = ByteCursor(raw)
        val count = cursor.readDigits(2)
        require(count in 0..99) { "Invalid Function Code Count: $count" }
        if (count == 0) return emptyList()
        val result = ArrayList<FunctionCodeData>(count)
        repeat(count) {
            val code = cursor.readAscii(3)
            require(code.all { it.isDigit() }) { "Invalid Function Code: $code" }
            val length = cursor.readDigits(3)
            require(length in 0..999) { "Invalid Data Length for Function Code $code" }
            val data = cursor.readLatin1(length)
            result.add(FunctionCodeData(code = code, data = data))
        }
        require(cursor.remaining == 0) {
            "Field 63 has ${cursor.remaining} leftover byte(s) after function codes"
        }
        return result
    }

    /**
     * اگر سوئیچ برای یک Function Code تنها، طول n3 را نگذارد:
     * Count(n2) + Code(n3) + Data(بقیه).
     */
    private fun parseSingleWithoutDataLength(raw: ByteArray): List<FunctionCodeData> {
        val cursor = ByteCursor(raw)
        val count = cursor.readDigits(2)
        require(count == 1) {
            "Field 63 without per-item length is only valid for a single function code"
        }
        val code = cursor.readAscii(3)
        require(code.all { it.isDigit() }) { "Invalid Function Code: $code" }
        val data = cursor.readLatin1(cursor.remaining)
        return listOf(FunctionCodeData(code = code, data = data))
    }
}

internal class ByteCursor(private val raw: ByteArray) {
    var index: Int = 0
        private set

    fun has(length: Int): Boolean = length >= 0 && index + length <= raw.size

    val remaining: Int get() = raw.size - index

    fun readAscii(length: Int): String {
        require(length >= 0 && index + length <= raw.size) {
            "Not enough Field 63 data. index=$index length=$length size=${raw.size}"
        }
        val value = String(raw, index, length, SadadField63Wire.CHARSET)
        index += length
        return value
    }

    fun readDigits(length: Int): Int {
        val text = readAscii(length).trim()
        return text.toIntOrNull()
            ?: throw IllegalArgumentException("Expected $length digit(s), got '$text'")
    }

    fun readLatin1(length: Int): String = readAscii(length)

    fun readBytes(length: Int): ByteArray {
        require(length >= 0 && index + length <= raw.size) {
            "Not enough data. index=$index length=$length size=${raw.size}"
        }
        val value = raw.copyOfRange(index, index + length)
        index += length
        return value
    }
}
