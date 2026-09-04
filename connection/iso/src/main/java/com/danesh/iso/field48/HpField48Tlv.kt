package com.danesh.iso.field48

import java.nio.charset.Charset
import java.util.Locale

class HpField48Tlv : Field48Tlv {

    private val map = linkedMapOf<String, String>()

    override fun addNode(key: String, value: String): Field48Tlv {
        map[formatTag(key)] = value
        return this
    }

    override fun getNode(key: String): String? =
        map[formatTag(key)]

    override fun packText(): String {
        val sb = StringBuilder()

        map.forEach { (tag, value) ->
            val valueBytes = value.toByteArray(CP1256)

            sb.append(formatTag(tag))
            //sb.append("%03d".format(valueBytes.size))
            sb.append(String.format(Locale.US, "%03d", valueBytes.size))
            sb.append(value)
        }

        return sb.toString()
    }
override fun pack(): ByteArray {
    val sb = StringBuilder()
    map.forEach { (tag, value) ->
        val valueBytes = value.toByteArray(CP1256)
        sb.append(tag)
        sb.append(String.format(Locale.US, "%03d", valueBytes.size))
        sb.append(value)
    }

    return sb.toString().toByteArray(CP1256)
}



    override fun unpack(data: String) {
        unpack(data.toByteArray(CP1256))
    }

    override fun clear() {
        map.clear()
    }
    override fun unpack(bytes: ByteArray) {
        map.clear()

        var index = 0

        while (index + TAG_LENGTH + LENGTH_SIZE <= bytes.size) {

            val tag = String(bytes, index, TAG_LENGTH, CP1256)

            val lenText = String(
                bytes,
                index + TAG_LENGTH,
                LENGTH_SIZE,
                CP1256
            )

            val length = lenText.toIntOrNull() ?: break

            val valueStart = index + TAG_LENGTH + LENGTH_SIZE
            val valueEnd = valueStart + length

            if (valueEnd > bytes.size) break

            val value = String(
                bytes,
                valueStart,
                length,
                CP1256
            )

            map[tag] = value

            index = valueEnd
        }
    }

    private fun formatTag(key: String): String {
        val digits = key.trim().trimStart('0').ifEmpty { "0" }
        return digits.padStart(TAG_LENGTH, '0')
    }

    companion object {
        private val CP1256 = Charset.forName("cp1256")

        private const val TAG_LENGTH = 3
        private const val LENGTH_SIZE = 3
    }
}