package com.danesh.sadad.field54

/**
 * DE54 سداد، ۴۰ بایت.
 *
 * نمونه هگز:
 * `30313031333634433030303030303434373630343031303233363444303030303030343337363034`
 * که ASCII آن است:
 * `0101364C0000004476040102364D000000437604`
 *
 * دو عدد ۱۲ رقمی، به ترتیب: مانده واقعی، سپس مانده در دسترس.
 */
data class SadadField54Balances(
    val actual: String,
    val available: String?,
)

object SadadField54Parser {
    private val signedAmount = Regex("[CD](\\d{12})")

    fun parse(raw: String?): SadadField54Balances? {
        val ascii = toAscii(raw)
        val amounts = signedAmount.findAll(ascii)
            .map { it.groupValues[1].trimStart('0').ifEmpty { "0" } }
            .toList()
        if (amounts.isEmpty()) return null
        return SadadField54Balances(
            actual = amounts[0],
            available = amounts.getOrNull(1),
        )
    }

    private fun toAscii(raw: String?): String {
        val compact = raw?.filter { !it.isWhitespace() }.orEmpty()
        if (compact.length % 2 == 0 && compact.length >= 2 && compact.all { it.isHex() }) {
            val decoded = compact.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
                .toString(Charsets.US_ASCII)
            if (signedAmount.containsMatchIn(decoded)) return decoded
        }
        return compact
    }

    private fun Char.isHex(): Boolean =
        this in '0'..'9' || this in 'A'..'F' || this in 'a'..'f'
}
