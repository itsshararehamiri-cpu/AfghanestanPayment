package com.danesh.bp.support

import com.danesh.api.SupportMenuItem

/**
 * پارس تگ ۲۳ فیلد ۴۸ الماس.
 *
 * فرمت اصلی: `id;title;amount|id;title;amount`
 * فرمت قدیمی برای سازگاری: `id*title*amount#id*title*amount`
 */
object BpSupportMenuParser {

    fun parse(raw: String?): List<SupportMenuItem> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return emptyList()

        val records = when {
            text.contains('|') && text.contains(';') -> text.split('|')
            text.contains('#') -> text.split('#')
            else -> listOf(text)
        }

        return records.mapIndexedNotNull { index, record ->
            parseRecord(record.trim(), fallbackId = (index + 1).toString())
        }
    }

    private fun parseRecord(record: String, fallbackId: String): SupportMenuItem? {
        if (record.isEmpty()) return null
        val parts = when {
            record.contains(';') -> record.split(';')
            record.contains('*') -> record.split('*')
            record.contains(',') -> record.split(',')
            else -> return SupportMenuItem(
                serviceId = fallbackId,
                title = record,
                amount = "0",
            )
        }.map { it.trim() }.filter { it.isNotEmpty() }

        return when (parts.size) {
            1 -> SupportMenuItem(serviceId = fallbackId, title = parts[0], amount = "0")
            2 -> SupportMenuItem(serviceId = fallbackId, title = parts[0], amount = digits(parts[1]))
            else -> SupportMenuItem(
                serviceId = parts[0],
                title = parts[1],
                amount = digits(parts[2]),
            )
        }
    }

    private fun digits(value: String): String = value.filter(Char::isDigit).ifEmpty { "0" }
}
