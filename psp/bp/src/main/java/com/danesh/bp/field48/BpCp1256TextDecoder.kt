package com.danesh.bp.field48

import java.nio.charset.Charset


object BpCp1256TextDecoder {
    private val CP1256: Charset = Charset.forName("cp1256")

    fun decode(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return trimmed

        if (trimmed.any { it in ARABIC_SCRIPT_RANGE }) return trimmed
        if (trimmed.all { it.code in ASCII_PRINTABLE }) return trimmed

        return runCatching {
            String(trimmed.toByteArray(Charsets.ISO_8859_1), CP1256).trim()
        }.getOrElse { trimmed }
    }

    private val ARABIC_SCRIPT_RANGE = '\u0600'..'\u06FF'
    private val ASCII_PRINTABLE = 0x20..0x7E
}
