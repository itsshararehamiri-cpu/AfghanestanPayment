package com.danesh.core.emv

/**
 * ابزار سبک BER-TLV برای پاسخ‌های APDU کارت (معادل توابع TLV در kahroba.c).
 *
 * تگ‌ها به‌صورت عدد صحیح نگه داشته می‌شوند؛ مثلاً 9F26 → 0x9F26 و 57 → 0x57.
 */
data class Tlv(val tag: Int, val value: ByteArray, val children: List<Tlv> = emptyList()) {
    override fun equals(other: Any?): Boolean =
        other is Tlv && tag == other.tag && value.contentEquals(other.value) && children == other.children

    override fun hashCode(): Int = 31 * (31 * tag + value.contentHashCode()) + children.hashCode()
}

object TlvParser {

    /** تجزیهٔ بازگشتی؛ در صورت داده‌ی خراب، آنچه تا آن‌جا معتبر بوده برگردانده می‌شود. */
    fun parse(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): List<Tlv> {
        val result = mutableListOf<Tlv>()
        var i = offset
        val end = (offset + length).coerceAtMost(data.size)
        while (i < end) {
            val first = data[i].toInt() and 0xFF
            // بایت‌های پرکننده بین TLVها
            if (first == 0x00 || first == 0xFF) {
                i++
                continue
            }
            var tag = first
            i++
            if (first and 0x1F == 0x1F) {
                while (i < end) {
                    val b = data[i].toInt() and 0xFF
                    tag = (tag shl 8) or b
                    i++
                    if (b and 0x80 == 0) break
                }
            }
            if (i >= end) break
            var len = data[i].toInt() and 0xFF
            i++
            if (len and 0x80 != 0) {
                val count = len and 0x7F
                if (count == 0 || count > 3 || i + count > end) break
                len = 0
                repeat(count) {
                    len = (len shl 8) or (data[i].toInt() and 0xFF)
                    i++
                }
            }
            if (i + len > end) break
            val value = data.copyOfRange(i, i + len)
            val constructed = firstByteOf(tag) and 0x20 != 0
            val children = if (constructed) parse(value) else emptyList()
            result += Tlv(tag, value, children)
            i += len
        }
        return result
    }

    /** اولین مقدار تگ در هر عمقی از درخت TLV. */
    fun find(data: ByteArray, tag: Int): ByteArray? = find(parse(data), tag)

    fun find(list: List<Tlv>, tag: Int): ByteArray? {
        for (item in list) {
            if (item.tag == tag) return item.value
            find(item.children, tag)?.let { return it }
        }
        return null
    }

    /**
     * جستجوی خطی تگ مثل getDataByTag در نسخهٔ C؛ فقط به‌عنوان پشتیبان وقتی ساختار TLV استاندارد نیست.
     */
    fun scan(data: ByteArray, tag: Int): ByteArray? {
        val tagBytes = tagBytes(tag)
        var i = 0
        while (i + tagBytes.size < data.size) {
            var match = true
            for (j in tagBytes.indices) {
                if (data[i + j] != tagBytes[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                val lenPos = i + tagBytes.size
                val len = data[lenPos].toInt() and 0xFF
                if (len < 0x80 && lenPos + 1 + len <= data.size) {
                    return data.copyOfRange(lenPos + 1, lenPos + 1 + len)
                }
            }
            i++
        }
        return null
    }

    /** تجزیهٔ DOL (مثل CDOL1): فهرست جفت‌های (تگ، طول). */
    fun parseDol(dol: ByteArray): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var i = 0
        while (i < dol.size) {
            val first = dol[i].toInt() and 0xFF
            var tag = first
            i++
            if (first and 0x1F == 0x1F) {
                while (i < dol.size) {
                    val b = dol[i].toInt() and 0xFF
                    tag = (tag shl 8) or b
                    i++
                    if (b and 0x80 == 0) break
                }
            }
            if (i >= dol.size) break
            val len = dol[i].toInt() and 0xFF
            i++
            result += tag to len
        }
        return result
    }

    fun tagBytes(tag: Int): ByteArray = when {
        tag > 0xFFFF -> byteArrayOf((tag shr 16).toByte(), (tag shr 8).toByte(), tag.toByte())
        tag > 0xFF -> byteArrayOf((tag shr 8).toByte(), tag.toByte())
        else -> byteArrayOf(tag.toByte())
    }

    private fun firstByteOf(tag: Int): Int = when {
        tag > 0xFFFF -> (tag shr 16) and 0xFF
        tag > 0xFF -> (tag shr 8) and 0xFF
        else -> tag and 0xFF
    }
}

/** سازندهٔ TLV (معادل add_emv_tag). مقدار خالی نادیده گرفته می‌شود. */
class TlvBuilder {
    private val out = java.io.ByteArrayOutputStream()

    fun add(tag: Int, value: ByteArray?): TlvBuilder {
        if (value == null || value.isEmpty()) return this
        out.write(TlvParser.tagBytes(tag))
        val len = value.size
        when {
            len < 0x80 -> out.write(len)
            len <= 0xFF -> {
                out.write(0x81)
                out.write(len)
            }
            else -> {
                out.write(0x82)
                out.write(len shr 8)
                out.write(len and 0xFF)
            }
        }
        out.write(value)
        return this
    }

    fun build(): ByteArray = out.toByteArray()
}

internal fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it.toInt() and 0xFF) }

internal fun String.hexToBytes(): ByteArray {
    val clean = filter { !it.isWhitespace() }
    require(clean.length % 2 == 0) { "hex length must be even" }
    return ByteArray(clean.length / 2) { i -> clean.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}
