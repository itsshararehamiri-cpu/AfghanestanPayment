package com.danesh.common.receipt

fun formatStanRrnDisplay(stan: String, rrn: String?): String {
    val cleanStan = stan.trim()
    val cleanRrn = rrn?.trim().orEmpty()
    return when {
        cleanStan.isNotEmpty() && cleanRrn.isNotEmpty() -> "$cleanStan/$cleanRrn"
        cleanStan.isNotEmpty() -> cleanStan
        cleanRrn.isNotEmpty() -> cleanRrn
        else -> ""
    }
}
