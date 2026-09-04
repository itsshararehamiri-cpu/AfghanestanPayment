package com.danesh.common.locale

import androidx.annotation.StringRes
import com.danesh.common.R

/**
 * زبان‌های اپ.
 *
 * زبان پیش‌فرض هر PSP در لایه app (DefaultAppLanguageMapping) تعریف می‌شود.
 * [default] فقط fallback برای parse نامعتبر است.
 */
enum class AppLanguage(
    @StringRes val labelRes: Int,
    val localeTag: String,
    val layoutDirection: AppLayoutDirection,
) {
    PersianDari(R.string.lang_persian_dari, "fa-AF", AppLayoutDirection.Rtl),
    PersianPashto(R.string.lang_persian_pashto, "ps-AF", AppLayoutDirection.Rtl),
    Other(R.string.lang_persian_iranian, "fa-IR", AppLayoutDirection.Rtl),
    Persian(R.string.lang_persian_iranian, "fa-IR", AppLayoutDirection.Rtl),
    English(R.string.lang_english, "en-US", AppLayoutDirection.Ltr);

    companion object {
        val default: AppLanguage = Persian

        fun fromName(name: String?): AppLanguage =
            entries.find { it.name == name } ?: default

        fun fromTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return default
            entries.find { it.localeTag.equals(tag, ignoreCase = true) }?.let { return it }
            return when (tag.substringBefore('-').lowercase()) {
                "en" -> English
                "ps" -> PersianPashto
                "fa" -> if (tag.contains("IR", ignoreCase = true)) Other else PersianDari
                else -> default
            }
        }
    }
}
