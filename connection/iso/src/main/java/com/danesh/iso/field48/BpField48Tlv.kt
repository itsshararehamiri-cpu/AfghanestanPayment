package com.danesh.iso.field48

import java.nio.charset.Charset
import java.util.Locale


class BpField48Tlv : Field48Tlv {
    private val map = linkedMapOf<String, String>()

    override fun addNode(key: String, value: String): Field48Tlv {
        map[formatTag(key)] = value
        return this
    }

    override fun getNode(key: String): String? = map[formatTag(key)]

    override fun packText(): String {
        val s = StringBuilder()
        map.forEach { (tag, value) ->
            val valueBytes = value.toByteArray(CP1256)
            val len = valueBytes.size.coerceIn(0, maxValueLength())
            s.append(
                String.format(
                    Locale.US,
                    "%s%03d%s",
                    formatTag(tag),
                    len,
                    String(valueBytes, CP1256),
                ).toEnglishNumber(),
            )
        }
        return s.toString()
    }

    override fun pack(): ByteArray = packText().toByteArray(CP1256)

    override fun unpack(data: String) {
        try {
            map.clear()
            val normalized = data.toEnglishNumber()
            var index = 0
            val headerSize = TAG_LENGTH + LENGTH_SIZE
            while (index + headerSize <= normalized.length) {
                val tag = normalized.substring(index, index + TAG_LENGTH)
                val len = normalized
                    .substring(index + TAG_LENGTH, index + headerSize)
                    .toIntOrNull()
                    ?: break
                if (index + headerSize + len > normalized.length) break
                val value = normalized.substring(index + headerSize, index + headerSize + len)
                map[tag] = value
                index += headerSize + len
            }
        } catch (e: Exception) {
        }
    }

    override fun unpack(bytes: ByteArray) {
        try {
            map.clear()
            var index = 0
            val headerSize = TAG_LENGTH + LENGTH_SIZE
            while (index + headerSize <= bytes.size) {
                val tag = String(bytes, index, TAG_LENGTH, Charsets.US_ASCII)
                val len = String(bytes, index + TAG_LENGTH, LENGTH_SIZE, Charsets.US_ASCII)
                    .toEnglishNumber()
                    .toIntOrNull()
                    ?: break
                val valueStart = index + headerSize
                val valueEnd = valueStart + len
                if (valueEnd > bytes.size) break
                map[tag] = String(bytes, valueStart, len, CP1256)
                index = valueEnd
            }
        } catch (e: Exception) {
        }
    }

    override fun clear() {

    }
    protected open fun formatTag(key: String): String {
        val normalized = key.trim().uppercase().toEnglishNumber()
        if (normalized.length == TAG_LENGTH && normalized.all(::isHexChar)) {
            return normalized
        }
        return when {
            normalized.length >= TAG_LENGTH -> normalized.takeLast(TAG_LENGTH)
            else -> normalized.padStart(TAG_LENGTH, '0')
        }
    }

    protected open fun formatLength(length: Int): String =
        "%0${LENGTH_SIZE}d"
            .format(Locale.US, length.coerceIn(0, maxValueLength()))
            .toEnglishNumber()

    protected open fun maxValueLength(): Int {
        var max = 0
        repeat(LENGTH_SIZE) {
            max = max * 10 + 9
        }
        return max
    }

    private fun isHexChar(char: Char): Boolean =
        char.isDigit() || char in 'A'..'F'

    private fun String.toEnglishNumber(): String {
        if (isEmpty()) return this
        val out = StringBuilder(length)
        for (c in this) {
            out.append(
                when (c) {
                    in '۰'..'۹' -> ('0'.code + (c - '۰')).toChar()
                    in '٠'..'٩' -> ('0'.code + (c - '٠')).toChar()
                    else -> c
                },
            )
        }
        return out.toString()
    }

    companion object {
        private val CP1256 = Charset.forName("cp1256")
        private const val TAG = "BpField48Tlv"
        const val TAG_LENGTH = 2
        const val LENGTH_SIZE = 3
    }
}
