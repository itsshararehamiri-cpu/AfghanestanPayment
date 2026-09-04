package com.danesh.bp.key

import android.util.Log
import org.jpos.iso.ISOUtil

internal object BpKeyParser {

    fun parseKeyMaterial(raw: ByteArray): ByteArray {
        val text = raw.toString(Charsets.UTF_8).trim()
        if (text.length >= 16 && text.length % 2 == 0 && text.all(::isHexChar)) {
            return runCatching { text.decodeHexKey() }.getOrDefault(raw)
        }
        return raw
    }

    private fun isHexChar(char: Char): Boolean =
        char.isDigit() || char in 'A'..'F' || char in 'a'..'f'
}
