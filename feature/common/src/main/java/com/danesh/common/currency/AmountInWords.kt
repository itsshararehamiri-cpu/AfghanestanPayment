package com.danesh.common.currency

import com.danesh.common.locale.AppLocale

/**
 * تبدیل مبلغ عددی به حروف (فارسی/دری و انگلیسی) برای نمایش زیر فیلد مبلغ.
 * پشتو فعلاً از متن فارسی استفاده می‌کند.
 */
object AmountInWords {

    private const val MAX_DIGITS = 15

    /** [amount] فقط ارقام (کاما و فاصله نادیده گرفته می‌شود)؛ برای ورودی خالی/نامعتبر `null`. */
    fun convert(amount: String, locale: AppLocale?): String? {
        val digits = amount.filter { !it.isWhitespace() && it != ',' && it != '٬' }
            .map { it.toAsciiDigit() ?: return null }
            .joinToString("")
            .trimStart('0')
        if (amount.isBlank()) return null
        if (digits.isEmpty()) return if (locale == AppLocale.ENGLISH) "zero" else "صفر"
        if (digits.length > MAX_DIGITS) return null
        val value = digits.toLong()
        return if (locale == AppLocale.ENGLISH) english(value) else persian(value)
    }

    private fun Char.toAsciiDigit(): Char? = when (this) {
        in '0'..'9' -> this
        in '۰'..'۹' -> '0' + (this - '۰')
        in '٠'..'٩' -> '0' + (this - '٠')
        else -> null
    }

    // region Persian

    private val faOnes = arrayOf(
        "", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه",
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده",
    )
    private val faTens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val faHundreds = arrayOf(
        "", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد",
    )
    private val faScales = arrayOf("", "هزار", "میلیون", "میلیارد", "هزار میلیارد")

    private fun persian(value: Long): String =
        groups(value).mapIndexedNotNull { scale, group ->
            if (group == 0) return@mapIndexedNotNull null
            val words = persianBelowThousand(group)
            if (scale == 0) words else "$words ${faScales[scale]}"
        }.reversed().joinToString(" و ")

    private fun persianBelowThousand(n: Int): String {
        val parts = mutableListOf<String>()
        if (n >= 100) parts += faHundreds[n / 100]
        val rest = n % 100
        when {
            rest in 1..19 -> parts += faOnes[rest]
            rest >= 20 -> {
                parts += faTens[rest / 10]
                if (rest % 10 != 0) parts += faOnes[rest % 10]
            }
        }
        return parts.joinToString(" و ")
    }

    // endregion

    // region English

    private val enOnes = arrayOf(
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
        "eighteen", "nineteen",
    )
    private val enTens = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
    )
    private val enScales = arrayOf("", "thousand", "million", "billion", "trillion")

    private fun english(value: Long): String =
        groups(value).mapIndexedNotNull { scale, group ->
            if (group == 0) return@mapIndexedNotNull null
            val words = englishBelowThousand(group)
            if (scale == 0) words else "$words ${enScales[scale]}"
        }.reversed().joinToString(" ")

    private fun englishBelowThousand(n: Int): String {
        val parts = mutableListOf<String>()
        if (n >= 100) parts += "${enOnes[n / 100]} hundred"
        val rest = n % 100
        when {
            rest in 1..19 -> parts += enOnes[rest]
            rest >= 20 -> parts += if (rest % 10 == 0) {
                enTens[rest / 10]
            } else {
                "${enTens[rest / 10]}-${enOnes[rest % 10]}"
            }
        }
        return parts.joinToString(" ")
    }

    // endregion

    /** گروه‌های سه‌رقمی از کم‌ارزش به پرارزش. */
    private fun groups(value: Long): List<Int> {
        val result = mutableListOf<Int>()
        var remaining = value
        while (remaining > 0) {
            result += (remaining % 1000).toInt()
            remaining /= 1000
        }
        return result
    }
}
