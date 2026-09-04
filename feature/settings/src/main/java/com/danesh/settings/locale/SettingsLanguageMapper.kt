package com.danesh.settings.locale

import com.danesh.common.locale.AppLanguage as CoreAppLanguage
import com.danesh.settings.model.AppLanguage

fun CoreAppLanguage.toSettingsLanguage(): AppLanguage = when (this) {
    CoreAppLanguage.PersianDari -> AppLanguage.PersianDari
    CoreAppLanguage.PersianPashto -> AppLanguage.PersianPashto
    CoreAppLanguage.Other -> AppLanguage.Other
    CoreAppLanguage.English -> AppLanguage.English
    CoreAppLanguage.Persian -> AppLanguage.Persian
}

fun AppLanguage.toCoreLanguage(): CoreAppLanguage = when (this) {
    AppLanguage.PersianDari -> CoreAppLanguage.PersianDari
    AppLanguage.PersianPashto -> CoreAppLanguage.PersianPashto
    AppLanguage.Other -> CoreAppLanguage.Other
    AppLanguage.Persian -> CoreAppLanguage.Other
    AppLanguage.English -> CoreAppLanguage.English
}
