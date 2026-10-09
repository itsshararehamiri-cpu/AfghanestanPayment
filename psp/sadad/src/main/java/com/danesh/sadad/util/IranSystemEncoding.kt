package com.danesh.sadad.util

/**
 * تبدیل ایران‌سیستم (کدپیج ترمینال سداد / `irsystem2Utf8`) به UTF-8.
 * بایت‌های زیر 0x80 همان ASCII هستند.
 *
 * ایران‌سیستم شکل‌های جدا/چسبان هر حرف را جدا کد می‌کند؛ اینجا همه به حرف پایه نگاشت می‌شوند.
 * نگاشت با نمونهٔ واقعی Function Code 029 سداد تطبیق داده شده
 * (`A1`=خ، `A2`=د، `A4`=ر، `EE`=ک، `F2`=لا، `FF`=فاصله).
 * نمونهٔ سداد: `96A897` → «تست».
 */
object IranSystemEncoding {
    private val HIGH: Array<String> = arrayOf(
        /*80*/ "۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷",
        /*88*/ "۸", "۹", "،", "ـ", "؟", "آ", "ئ", "ء",
        /*90*/ "ا", "ا", "ب", "ب", "پ", "پ", "ت", "ت",
        /*98*/ "ث", "ث", "ج", "ج", "چ", "چ", "ح", "ح",
        /*A0*/ "خ", "خ", "د", "ذ", "ر", "ز", "ژ", "س",
        /*A8*/ "س", "ش", "ش", "ص", "ص", "ض", "ض", "ط",
        // B0–DF در ایران‌سیستم استاندارد کاراکترهای جعبه‌ای DOS هستند؛ برای سازگاری با
        // پیاده‌سازی قبلی به حروف نگاشت می‌شوند تا متن خراب نشود.
        /*B0*/ "ط", "ط", "ظ", "ظ", "ع", "ع", "غ", "غ",
        /*B8*/ "ف", "ف", "ق", "ق", "ک", "ک", "گ", "گ",
        /*C0*/ "ل", "ل", "م", "م", "ن", "ن", "و", "و",
        /*C8*/ "ه", "ه", "ی", "ی", "چ", "چ", "ژ", "ژ",
        /*D0*/ "ط", "ظ", "ع", "غ", "ف", "ق", "ک", "ل",
        /*D8*/ "م", "ن", "ه", "و", "ی", "ی", " ", " ",
        /*E0*/ "ظ", "ع", "ع", "ع", "ع", "غ", "غ", "غ",
        /*E8*/ "غ", "ف", "ف", "ق", "ق", "ک", "ک", "گ",
        /*F0*/ "گ", "ل", "لا", "ل", "م", "م", "ن", "ن",
        /*F8*/ "و", "ه", "ه", "ه", "ی", "ی", "ی", " ",
    )

    fun toUtf8(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val out = StringBuilder(bytes.size)
        for (b in bytes) {
            val u = b.toInt() and 0xFF
            if (u < 0x80) out.append(u.toChar()) else out.append(HIGH[u - 0x80])
        }
        return out.toString()
    }

    /**
     * متن «بصری» ایران‌سیستم (چپ‌به‌راست ذخیره‌شده، مثل Function Code 029) را به ترتیب منطقی برمی‌گرداند:
     * بایت‌ها معکوس و سپس تبدیل می‌شوند (ارقام هم در نمونهٔ سداد معکوس آمده‌اند).
     */
    fun visualToUtf8(bytes: ByteArray): String = toUtf8(bytes.reversedArray()).trim()

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

    internal fun decodeAsciiHex(raw: ByteArray): ByteArray? {
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
