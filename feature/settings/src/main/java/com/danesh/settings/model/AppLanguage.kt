package com.danesh.settings.model

import androidx.annotation.StringRes
import com.danesh.common.locale.AppLayoutDirection
import com.danesh.settings.R

enum class AppLanguage(
    @StringRes val labelRes: Int,
    val layoutDirection: AppLayoutDirection,
) {
    PersianDari(R.string.settings_language_persian_dari, AppLayoutDirection.Rtl),
    PersianPashto(R.string.settings_language_persian_pashto, AppLayoutDirection.Rtl),
    Other(R.string.settings_language_other, AppLayoutDirection.Rtl),
    Persian(R.string.settings_language_persian, AppLayoutDirection.Rtl),
    English(R.string.settings_language_english, AppLayoutDirection.Ltr),
}
