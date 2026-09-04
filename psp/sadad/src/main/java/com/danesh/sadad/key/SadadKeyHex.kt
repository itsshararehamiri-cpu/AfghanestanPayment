package com.danesh.sadad.key

internal fun String.decodeHexKey(): ByteArray {
    val clean = trim()
    require(clean.length % 2 == 0) { "Invalid hex key length" }
    return ByteArray(clean.length / 2) { index ->
        clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }
}
