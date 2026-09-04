package com.danesh.common.locale

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsLocalePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val defaultAppLanguageProvider: DefaultAppLanguageProvider,
) : LocalePreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getLanguage(): AppLanguage {
        prefs.getString(KEY_LOCALE_TAG, null)?.let { tag ->
            return AppLanguage.fromTag(tag)
        }

        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val legacyName = legacyPrefs.getString(LEGACY_KEY_LANGUAGE, null)
        if (legacyName != null) {
            val language = AppLanguage.fromName(legacyName)
            setLanguage(language)
            legacyPrefs.edit().remove(LEGACY_KEY_LANGUAGE).apply()
            return language
        }
        return defaultAppLanguageProvider.getDefaultLanguage()
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LOCALE_TAG, language.localeTag).apply()
    }

    override fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "app_locale_prefs"
        private const val KEY_LOCALE_TAG = "locale_tag"
        private const val LEGACY_PREFS_NAME = "app_language_prefs"
        private const val LEGACY_KEY_LANGUAGE = "selected_language"
    }
}
