package com.danesh.voucher.ui

import java.util.Locale

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun normalizeDigits(value: String): String = buildString(value.length) {
    value.forEach { char ->
        when (char) {
            in '0'..'9' -> append(char)
            in '۰'..'۹' -> append('0' + (char - '۰'))
            in '٠'..'٩' -> append('0' + (char - '٠'))
        }
    }
}

fun toPersianDigits(value: String): String = buildString(value.length) {
    value.forEach { char ->
        append(
            when (char) {
                in '0'..'9' -> persianDigits[char - '0']
                else -> char
            },
        )
    }
}

fun formatAmount(amount: Int): String =
    String.format(Locale.US, "%,d", amount)

fun parseAmount(value: String): Int =
    normalizeDigits(value).toIntOrNull() ?: 0

fun amountToDisplay(value: String): String {
    val digits = normalizeDigits(value)
    if (digits.isEmpty()) return ""
    return formatAmount(digits.toIntOrNull() ?: return "")
}

fun formatMobileInput(value: String): String {
    val digits = normalizeDigits(value).take(11)//
    return toPersianDigits(digits)// TODO: 10
}

fun mobileNumberForSubmit(value: String): String =
    normalizeDigits(value)
