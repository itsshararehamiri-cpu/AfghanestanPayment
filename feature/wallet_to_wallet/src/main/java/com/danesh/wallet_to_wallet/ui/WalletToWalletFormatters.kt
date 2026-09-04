package com.danesh.wallet_to_wallet.ui

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

fun walletNumberForSubmit(value: String): String =
    normalizeDigits(value)

fun walletNumberToDisplay(value: String): String =
    normalizeDigits(value)
        .take(16)
        .chunked(4)
        .joinToString(" ")
