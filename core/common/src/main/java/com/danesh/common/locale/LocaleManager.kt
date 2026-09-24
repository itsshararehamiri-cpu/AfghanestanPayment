package com.danesh.common.locale

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {

    @Volatile
    private var appContext: Context? = null

    /** در Application.onCreate صدا زده می‌شود تا رشته‌های ApplicationContext هم هم‌زبان اپ شوند. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun apply(language: AppLanguage) {
        val locales = LocaleListCompat.forLanguageTags(language.localeTag)

        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
        appContext?.let { syncApplicationResources(it, language) }
    }

    /**
     * زیر اندروید ۱۳، AppCompat فقط Activityها را بومی می‌کند و پیام‌هایی که از
     * ApplicationContext خوانده می‌شوند (خطاها، پاسخ سوئیچ و ...) به زبان سیستم می‌آمدند.
     * Locale.setDefault عمداً تغییر نمی‌کند تا قالب‌بندی اعداد پیام‌های ISO دست نخورد.
     */
    @Suppress("DEPRECATION")
    private fun syncApplicationResources(context: Context, language: AppLanguage) {
        val locale = Locale.forLanguageTag(language.localeTag)
        val resources = context.resources
        if (resources.configuration.locales[0] == locale) return
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
