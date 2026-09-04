package com.danesh.common.locale

import saman.zamani.persiandate.PersianDate

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

private val pashtoSolarMonths = arrayOf(
    "وری", "غویی", "غبرګولی", "چنګاښ", "زمرګ", "وږی",
    "تله", "لړم", "لیندۍ", "مرغومی", "سلواغه", "کب",
)

object TransactionDateTimeFormatter {

    fun formatDate(
        dateYyyyMmDd: String,
        locale: AppLocale = AppLocale.default,
        calendarStyle: ReceiptCalendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
    ): String {
        val normalized = normalizeDateInput(dateYyyyMmDd)
            ?: return formatDigits(dateYyyyMmDd.filter(Char::isDigit).ifBlank { dateYyyyMmDd }, locale)
        if (calendarStyle == ReceiptCalendarStyle.GREGORIAN && !isShamsiYear(normalized)) {
            return formatGregorianDate(normalized, locale)
        }
        val solar = toSolarDate(normalized) ?: return formatDigits(normalized, locale)
        if (calendarStyle == ReceiptCalendarStyle.GREGORIAN) {
            val gregorian = toGregorianYyyyMmDd(solar) ?: normalized
            return formatGregorianDate(gregorian, locale)
        }
        return when (calendarStyle) {
            ReceiptCalendarStyle.IRANIAN_SHAMSI -> formatIranianShamsiDate(solar, locale)
            ReceiptCalendarStyle.AFGHAN_SOLAR -> formatAfghanSolarDate(solar, locale)
            ReceiptCalendarStyle.GREGORIAN -> error("handled above")
        }
    }

    fun formatTime(timeHhMmSs: String, locale: AppLocale = AppLocale.default): String {
        val normalized = timeHhMmSs.filter { it.isDigit() }
        if (normalized.length < 4) return formatDigits(timeHhMmSs, locale)

        val hours = normalized.take(2)
        val minutes = normalized.substring(2, 4)

        return formatDigits("$hours:$minutes", locale)
    }

    fun formatDisplay(
        dateYyyyMmDd: String,
        timeHhMmSs: String,
        locale: AppLocale = AppLocale.default,
        calendarStyle: ReceiptCalendarStyle = ReceiptCalendarStyle.AFGHAN_SOLAR,
    ): String = "${formatTime(timeHhMmSs, locale)} - ${formatDate(dateYyyyMmDd, locale, calendarStyle)}"

    fun toPersianDigits(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '0'..'9' -> persianDigits[char - '0']
                    else -> char
                },
            )
        }
    }

    private fun formatIranianShamsiDate(solar: SolarDate, locale: AppLocale): String {
        val year = formatDigits(solar.shYear.toString(), locale)
        val month = formatDigits(solar.shMonth.toString().padStart(2, '0'), locale)
        val day = formatDigits(solar.shDay.toString().padStart(2, '0'), locale)
        return "$year/$month/$day"
    }

    private fun formatAfghanSolarDate(solar: SolarDate, locale: AppLocale): String {
        val monthName = monthName(solar.shMonth, locale, solar.persianDate)
        return buildString {
            append(formatDigits(solar.shDay.toString(), locale))
            append(' ')
            append(monthName)
            append(' ')
            append(formatDigits(solar.shYear.toString(), locale))
        }
    }

    private fun formatGregorianDate(normalizedGregorian: String, locale: AppLocale): String {
        if (normalizedGregorian.length != 8) {
            return formatDigits(normalizedGregorian, locale)
        }
        val year = normalizedGregorian.substring(0, 4)
        val month = normalizedGregorian.substring(4, 6)
        val day = normalizedGregorian.substring(6, 8)
        return formatDigits("$year/$month/$day", locale)
    }

    private fun toGregorianYyyyMmDd(solar: SolarDate): String? {
        val date = solar.persianDate
        return "%04d%02d%02d".format(date.grgYear, date.grgMonth, date.grgDay)
    }

    private fun isShamsiYear(normalizedDate: String): Boolean {
        if (normalizedDate.length != 8) return false
        val year = normalizedDate.substring(0, 4).toIntOrNull() ?: return false
        return year in SHAMSI_YEAR_RANGE
    }

    private data class SolarDate(val persianDate: PersianDate, val shYear: Int, val shMonth: Int, val shDay: Int)

     fun normalizeDateInput(raw: String): String? {
        val digits = raw.filter(Char::isDigit)
        return when {
            digits.length >= 8 -> digits.take(8)
            digits.length == 6 -> "20${digits}"
            else -> null
        }
    }

    private fun toSolarDate(normalizedDate: String): SolarDate? {
        if (normalizedDate.length != 8) return null
        val year = normalizedDate.substring(0, 4).toIntOrNull() ?: return null
        val month = normalizedDate.substring(4, 6).toIntOrNull() ?: return null
        val day = normalizedDate.substring(6, 8).toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31) return null

        if (year in SHAMSI_YEAR_RANGE) {
            val persianDate = PersianDate().apply {
                shYear = year
                shMonth = month
                shDay = day
            }
            return SolarDate(
                persianDate = persianDate,
                shYear = year,
                shMonth = month,
                shDay = day,
            )
        }

        val persianDate = PersianDate().apply {
            grgYear = year
            grgMonth = month
            grgDay = day
        }
        return SolarDate(
            persianDate = persianDate,
            shYear = persianDate.shYear,
            shMonth = persianDate.shMonth,
            shDay = persianDate.shDay,
        )
    }

    private val SHAMSI_YEAR_RANGE = 1300..1499

    private fun monthName(shMonth: Int, locale: AppLocale, persianDate: PersianDate): String {
        val index = shMonth - 1
        return when (locale) {
            AppLocale.PASHTO -> pashtoSolarMonths.getOrElse(index) { "" }
            AppLocale.DARI,
            AppLocale.IRANIAN,
            AppLocale.ENGLISH,
            -> persianDate.monthName
        }
    }

    private fun formatDigits(value: String, locale: AppLocale): String =
        if (locale == AppLocale.ENGLISH) value else toPersianDigits(value)
}
