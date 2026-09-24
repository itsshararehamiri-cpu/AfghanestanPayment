package com.danesh.sadad.util

/**
 * تبدیل ایران‌سیستم (کدپیج ترمینال سداد / `irsystem2Utf8`) به UTF-8.
 * بایت‌های زیر 0x80 همان ASCII هستند.
 *
 * حروف متصل‌شونده در بازهٔ 0x90–0xBF به‌صورت جفت (جدا/چسبان) هستند.
 * نمونهٔ سداد: `96A897` → «تست».
 */
object IranSystemEncoding {
    private val HIGH: CharArray = charArrayOf(
        /*80*/ '۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷',
        /*88*/ '۸', '۹', '،', 'ـ', '؟', 'آ', 'ئ', 'ء',
        /*90*/ 'ا', 'ا', 'ب', 'ب', 'پ', 'پ', 'ت', 'ت',
        /*98*/ 'ث', 'ث', 'ج', 'ج', 'ح', 'ح', 'خ', 'خ',
        /*A0*/ 'د', 'د', 'ذ', 'ذ', 'ر', 'ر', 'ز', 'ز',
        /*A8*/ 'س', 'س', 'ش', 'ش', 'ص', 'ص', 'ض', 'ض',
        /*B0*/ 'ط', 'ط', 'ظ', 'ظ', 'ع', 'ع', 'غ', 'غ',
        /*B8*/ 'ف', 'ف', 'ق', 'ق', 'ک', 'ک', 'گ', 'گ',
        /*C0*/ 'ل', 'ل', 'م', 'م', 'ن', 'ن', 'و', 'و',
        /*C8*/ 'ه', 'ه', 'ی', 'ی', 'چ', 'چ', 'ژ', 'ژ',
        /*D0*/ 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ك', 'ل',
        /*D8*/ 'م', 'ن', 'ه', 'و', 'ى', 'ي', 'ً', 'ٌ',
        /*E0*/ 'ٍ', 'َ', 'ُ', 'ِ', 'ّ', 'ْ', 'پ', 'چ',
        /*E8*/ 'ژ', 'گ', 'ک', 'ی', '۰', '۱', '۲', '۳',
        /*F0*/ '۴', '۵', '۶', '۷', '۸', '۹', '،', '؛',
        /*F8*/ '؟', '٪', '٫', '٬', ' ', ' ', ' ', ' ',
    )

    fun toUtf8(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val out = StringBuilder(bytes.size)
        for (b in bytes) {
            val u = b.toInt() and 0xFF
            if (u < 0x80) {
                out.append(u.toChar())
            } else {
                out.append(HIGH[u - 0x80])
            }
        }
        return out.toString()
    }

    /**
     * دادهٔ فارسی روی سیم سداد گاهی هگز ASCII ایران‌سیستم است (`96A897`)،
     * گاهی بایت خام. مثل `utl_asciiToHex` + `irsystem2Utf8`.
     */
    fun fieldToUtf8(raw: ByteArray): String {
        if (raw.isEmpty()) return ""
        val hexDecoded = decodeAsciiHex(raw)
        val source = hexDecoded ?: raw
        return toUtf8(source).trim()
    }

    private fun decodeAsciiHex(raw: ByteArray): ByteArray? {
        if (raw.isEmpty() || raw.size % 2 != 0) return null
        if (raw.any { !it.toInt().toChar().isHexDigit() }) return null
        val out = ByteArray(raw.size / 2)
        var i = 0
        while (i < raw.size) {
            val hi = hexValue(raw[i])
            val lo = hexValue(raw[i + 1])
            if (hi < 0 || lo < 0) return null
            out[i / 2] = ((hi shl 4) or lo).toByte()
            i += 2
        }
        return out
    }

    private fun Char.isHexDigit(): Boolean =
        this in '0'..'9' || this in 'A'..'F' || this in 'a'..'f'

    private fun hexValue(b: Byte): Int {
        val c = (b.toInt() and 0xFF).toChar()
        return when (c) {
            in '0'..'9' -> c - '0'
            in 'A'..'F' -> c - 'A' + 10
            in 'a'..'f' -> c - 'a' + 10
            else -> -1
        }
    }
}
