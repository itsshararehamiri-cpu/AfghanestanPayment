package com.danesh.sadad.util

import android.util.Log
import com.danesh.api.HostPrintItem
import com.danesh.api.HostPrintTarget

/**
 * Host Function Code 029 (Print Data) سداد:
 *
 * `n2` Count، سپس برای هر آیتم:
 * `n2` Type (`01` = KEY/VALUE در یک سطر، `03` = هر مقدار در یک سطر جدا)
 * + `n1` Print Type (`1` = فقط رسید مشتری، `2` = فقط رسید پذیرنده، `3` = هر دو)
 * + `n2` Key Length + Key (ایران‌سیستم بصری)
 * + `n2` Value Length + Value (ایران‌سیستم بصری).
 *
 * نمونهٔ واقعی (کالابرگ): `03` `03 3 25 «پرداختی از کالابرگ (ریال):» 07 «200,000»` …
 *
 * پارسر هرگز exception بیرون نمی‌دهد: داده‌ٔ ناقص/خراب باعث می‌شود آیتم‌های سالم قبلی
 * برگردانده شوند و بقیه نادیده گرفته شود.
 */
object SadadPrintDataParser {
    private const val TAG = "SadadFC029"

    const val TYPE_KEY_VALUE = "01"
    const val TYPE_LINE = "03"

    fun parse(data: String?): List<HostPrintItem> {
        if (data.isNullOrEmpty()) return emptyList()
        return runCatching { parse(data.toByteArray(SadadField63Wire.CHARSET)) }
            .onFailure { Log.w(TAG, "FC029 parse failed: ${it.message}") }
            .getOrDefault(emptyList())
    }

    fun parse(raw: ByteArray): List<HostPrintItem> {
        val reader = SafeReader(raw)
        val count = reader.number(2) ?: return emptyList()
        val items = ArrayList<HostPrintItem>(count.coerceAtMost(MAX_ITEMS))
        for (index in 0 until count.coerceAtMost(MAX_ITEMS)) {
            val item = readItem(reader)
            if (item == null) {
                Log.w(TAG, "FC029 item #$index truncated/invalid; parsed ${items.size}/$count")
                break
            }
            if (item.caption.isNotBlank() || item.value.isNotBlank()) items += item
        }
        return items
    }

    private fun readItem(reader: SafeReader): HostPrintItem? {
        val type = reader.text(2) ?: return null
        val printType = reader.text(1) ?: return null
        val keyLength = reader.number(2) ?: return null
        val key = reader.bytes(keyLength) ?: return null
        val valueLength = reader.number(2) ?: return null
        val value = reader.bytes(valueLength) ?: return null
        return HostPrintItem(
            caption = IranSystemEncoding.visualToUtf8(key),
            value = IranSystemEncoding.visualToUtf8(value),
            keyValue = type != TYPE_LINE,
            target = when (printType) {
                "1" -> HostPrintTarget.CUSTOMER
                "2" -> HostPrintTarget.MERCHANT
                else -> HostPrintTarget.BOTH
            },
        )
    }

    private const val MAX_ITEMS = 99

    /** خواننده‌ای که به‌جای exception برای دادهٔ ناکافی/نامعتبر `null` برمی‌گرداند. */
    private class SafeReader(private val raw: ByteArray) {
        private var index = 0

        fun bytes(length: Int): ByteArray? {
            if (length < 0 || index + length > raw.size) return null
            return raw.copyOfRange(index, index + length).also { index += length }
        }

        fun text(length: Int): String? = bytes(length)?.let { String(it, SadadField63Wire.CHARSET) }

        fun number(length: Int): Int? = text(length)?.trim()?.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toInt()
    }
}
