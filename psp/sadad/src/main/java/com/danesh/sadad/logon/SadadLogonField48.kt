package com.danesh.sadad.logon

/**
 * DE48 پاسخ LOGON سداد:
 * TMS NEED (n1) + CHANGE_KEY NEED (n1) + در صورت غیرصفر بودن ستون دوم:
 * 3-DES PIN (ans32) + 3-DES MAC (ans32) + 3-DES DATA (ans32).
 * اگر CHANGE_KEY NEED صفر باشد بقیهٔ فیلد وجود ندارد.
 */
data class SadadLogonField48(
    val tmsNeed: Boolean,
    val changeKeyNeed: Boolean,
    val pinKey: String,
    val macKey: String,
    val dataKey: String,
)

object SadadLogonField48Parser {
    private const val KEY_LEN = 32
    private const val KEYS_LEN = KEY_LEN * 3

    fun parse(raw: String?): SadadLogonField48? {
        val field = raw.orEmpty()
        if (field.length < 2) return null
        val tmsDigit = field[0]
        val changeDigit = field[1]
        if (!tmsDigit.isDigit() || !changeDigit.isDigit()) return null
        val tmsNeed = tmsDigit != '0'
        val changeKeyNeed = changeDigit != '0'
        if (!changeKeyNeed) {
            return SadadLogonField48(
                tmsNeed = tmsNeed,
                changeKeyNeed = false,
                pinKey = "",
                macKey = "",
                dataKey = "",
            )
        }
        val keys = field.substring(2)
        return SadadLogonField48(
            tmsNeed = tmsNeed,
            changeKeyNeed = true,
            pinKey = keys.take(KEY_LEN),
            macKey = keys.drop(KEY_LEN).take(KEY_LEN),
            dataKey = keys.drop(KEY_LEN * 2).take(KEY_LEN),
        )
    }

    fun hasFullKeys(parsed: SadadLogonField48): Boolean =
        parsed.changeKeyNeed &&
            parsed.pinKey.length == KEY_LEN &&
            parsed.macKey.length == KEY_LEN &&
            parsed.dataKey.length == KEY_LEN

    const val EXPECTED_KEYS_LENGTH: Int = KEYS_LEN
}
