package com.danesh.common.locale

enum class AppLocale(val tag: String) {
    DARI("fa-AF"),

    PASHTO("ps-AF"),

    IRANIAN("fa-IR"),

    ENGLISH("en");


    companion object {
        val default: AppLocale = DARI

        fun fromTag(tag: String?): AppLocale {
            if (tag.isNullOrBlank()) return default
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) }?.let { return it }
            return when (tag.substringBefore('-').lowercase()) {
                "en" -> ENGLISH
                "ps" -> PASHTO
                "fa" -> if (tag.contains("IR", ignoreCase = true)) IRANIAN else DARI
                else -> default
            }
        }

        fun fromName(name: String?): AppLocale =
            runCatching { valueOf(name.orEmpty()) }.getOrNull()?.toAppLocale() ?: default
    }
}

fun String.toAppLocaleFromSettingsName(): AppLocale = when (this) {
    "PersianDari" -> AppLocale.DARI
    "PersianPashto" -> AppLocale.PASHTO
    "Other" -> AppLocale.IRANIAN
    "Persian" -> AppLocale.IRANIAN
    "English" -> AppLocale.ENGLISH
    else -> AppLocale.default
}

private fun Enum<*>.toAppLocale(): AppLocale = name.toAppLocaleFromSettingsName()

fun AppLanguage.toAppLocale(): AppLocale = when (this) {
    AppLanguage.English -> AppLocale.ENGLISH
    AppLanguage.PersianPashto -> AppLocale.PASHTO
    AppLanguage.PersianDari -> AppLocale.DARI
    AppLanguage.Persian,
    AppLanguage.Other,
    -> AppLocale.IRANIAN
}
