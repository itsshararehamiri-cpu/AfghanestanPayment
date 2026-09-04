package com.danesh.settings.util

enum class SettingsTextInputFilter {
    None,
    IpAddress,
    DigitsOnly,
    PositiveInteger,
}

object SettingsTextInputFilters {
    fun apply(filter: SettingsTextInputFilter, raw: String): String = when (filter) {
        SettingsTextInputFilter.None -> raw
        SettingsTextInputFilter.IpAddress -> normalizeDigits(raw).filter { it.isDigit() || it == '.' }
        SettingsTextInputFilter.DigitsOnly -> normalizeDigits(raw).filter { it.isDigit() }
        SettingsTextInputFilter.PositiveInteger -> filterPositiveInteger(normalizeDigits(raw))
    }

    fun ballotTicketDigitsOnly(raw: String): String =
        apply(SettingsTextInputFilter.DigitsOnly, raw)

    private fun normalizeDigits(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '0'..'9' -> char
                    in '۰'..'۹' -> ('0' + (char.code - '۰'.code)).toChar()
                    in '٠'..'٩' -> ('0' + (char.code - '٠'.code)).toChar()
                    else -> char
                },
            )
        }
    }

    private fun filterPositiveInteger(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return digits.trimStart('0')
    }

    fun isValidPositiveInteger(value: String): Boolean =
        value.toIntOrNull()?.let { it > 0 } == true
}
