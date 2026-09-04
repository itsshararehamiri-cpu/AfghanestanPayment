package com.danesh.common.locale

import androidx.annotation.StringRes
import com.danesh.common.R

@get:StringRes
val AppLanguage.labelRes: Int
    get() = when (this) {
        AppLanguage.PersianDari -> R.string.language_persian_dari
        AppLanguage.PersianPashto -> R.string.language_persian_pashto
        AppLanguage.Other -> R.string.language_other
        AppLanguage.English -> R.string.language_english
        AppLanguage.Persian -> R.string.lang_persian_iranian
    }
