package com.danesh.common.locale

interface LocalePreferences {
    fun getLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)
    fun resetToDefaults()
}
