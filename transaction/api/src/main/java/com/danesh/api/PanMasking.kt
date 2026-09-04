package com.danesh.api

fun String.maskPanForDisplay(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return ""
    if (trimmed.contains('*')) return trimmed

    val digits = trimmed.filter(Char::isDigit)
    if (digits.length < 10) return trimmed

    return digits.take(6) + "******" + digits.takeLast(4)
}
