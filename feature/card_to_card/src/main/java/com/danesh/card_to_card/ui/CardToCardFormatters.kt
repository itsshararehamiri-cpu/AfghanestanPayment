package com.danesh.card_to_card.ui

import java.util.Locale

fun normalizeDigits(value: String): String = buildString(value.length) {
    value.forEach { char ->
        when (char) {
            in '0'..'9' -> append(char)
            in '۰'..'۹' -> append('0' + (char - '۰'))
            in '٠'..'٩' -> append('0' + (char - '٠'))
        }
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

fun formatCardNumberInput(value: String): String =
    normalizeDigits(value)
        .take(16)
        .chunked(4)
        .joinToString(" ")

fun cardNumberForSubmit(value: String): String =
    normalizeDigits(value)

fun cardNumberToDisplay(value: String): String =
    formatCardNumberInput(value)
