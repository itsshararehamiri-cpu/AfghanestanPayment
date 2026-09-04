package com.danesh.settings.locale

import com.danesh.common.locale.AppLanguage as CoreAppLanguage
import com.danesh.settings.model.AppLanguage

interface SettingsLanguageOptions {
    fun availableSettingsLanguages(): List<AppLanguage>

    fun toSettingsLanguage(language: CoreAppLanguage): AppLanguage

    fun toCoreLanguage(language: AppLanguage): CoreAppLanguage

    /** Ensures a persisted language is valid for the active PSP build. */
    fun coerceCoreLanguage(language: CoreAppLanguage): CoreAppLanguage
}
