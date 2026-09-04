package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.AppLanguage
import com.danesh.settings.locale.SettingsLanguageOptions
import com.danesh.settings.model.AppLanguage as SettingsAppLanguage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PspSettingsLanguageOptions @Inject constructor(
    private val appRuntimeConfig: AppRuntimeConfig,
) : SettingsLanguageOptions {

    override fun availableSettingsLanguages(): List<SettingsAppLanguage> = when {
        appRuntimeConfig.activePsp.isBehpardakht -> behpardakhtLanguages()
        appRuntimeConfig.activePsp.isHamrahPay -> hamrahPayLanguages()
        else -> behpardakhtLanguages()
    }

    override fun toSettingsLanguage(language: AppLanguage): SettingsAppLanguage = when {
        appRuntimeConfig.activePsp.isBehpardakht -> when (language) {
            AppLanguage.Other -> SettingsAppLanguage.Persian
            AppLanguage.English -> SettingsAppLanguage.English
            else -> SettingsAppLanguage.Persian
        }
        // همراه‌پی: فقط دری / پشتو
        appRuntimeConfig.activePsp.isHamrahPay -> when (language) {
            AppLanguage.PersianPashto -> SettingsAppLanguage.PersianPashto
            else -> SettingsAppLanguage.PersianDari
        }
        else -> when (language) {
            AppLanguage.Other -> SettingsAppLanguage.Persian
            AppLanguage.English -> SettingsAppLanguage.English
            else -> SettingsAppLanguage.Persian
        }
    }

    override fun toCoreLanguage(language: SettingsAppLanguage): AppLanguage = when (language) {
        SettingsAppLanguage.Persian -> AppLanguage.Other
        SettingsAppLanguage.English -> AppLanguage.English
        SettingsAppLanguage.PersianDari -> AppLanguage.PersianDari
        SettingsAppLanguage.PersianPashto -> AppLanguage.PersianPashto
        SettingsAppLanguage.Other -> AppLanguage.Other
    }

    override fun coerceCoreLanguage(language: AppLanguage): AppLanguage = when {
        appRuntimeConfig.activePsp.isBehpardakht -> when (language) {
            AppLanguage.Persian,
            AppLanguage.Other,
            AppLanguage.English,
            -> language
            else -> appRuntimeConfig.activePsp.toDefaultAppLanguage()
        }
        appRuntimeConfig.activePsp.isHamrahPay -> when (language) {
            AppLanguage.PersianDari,
            AppLanguage.PersianPashto,
            -> language
            else -> appRuntimeConfig.activePsp.toDefaultAppLanguage()
        }
        else -> when (language) {
            AppLanguage.Other,
            AppLanguage.English,
            -> language
            else -> appRuntimeConfig.activePsp.toDefaultAppLanguage()
        }
    }

    /** به‌پرداخت: core Other (fa-IR) + English — در UI با برچسب فارسی/English */
    private fun behpardakhtLanguages() = listOf(
        SettingsAppLanguage.Persian,
        SettingsAppLanguage.English,
    )

    /** همراه‌پی: core PersianDari + PersianPashto */
    private fun hamrahPayLanguages() = listOf(
        SettingsAppLanguage.PersianDari,
        SettingsAppLanguage.PersianPashto, SettingsAppLanguage.English
    )
}
