package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.AppLanguage
import com.danesh.settings.model.AppLanguage as SettingsAppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PspSettingsLanguageOptionsTest {

    @Test
    fun behpardakht_offersOnlyPersianAndEnglish() {
        val options = optionsFor(ActivePsp.BP)

        assertEquals(
            listOf(SettingsAppLanguage.Persian, SettingsAppLanguage.English),
            options.availableSettingsLanguages(),
        )
        assertFalse(
            options.availableSettingsLanguages().contains(SettingsAppLanguage.PersianDari),
        )
        assertFalse(
            options.availableSettingsLanguages().contains(SettingsAppLanguage.PersianPashto),
        )
    }

    @Test
    fun behpardakht_coercesDariAndPashtoToPersian() {
        val options = optionsFor(ActivePsp.BP)

        assertEquals(AppLanguage.Other, options.coerceCoreLanguage(AppLanguage.PersianDari))
        assertEquals(AppLanguage.Other, options.coerceCoreLanguage(AppLanguage.PersianPashto))
    }

    @Test
    fun hamrahPay_offersOnlyDariAndPashto() {
        val options = optionsFor(ActivePsp.HP)

        assertEquals(
            listOf(
                SettingsAppLanguage.PersianDari,
                SettingsAppLanguage.PersianPashto,
            ),
            options.availableSettingsLanguages(),
        )
        assertFalse(
            options.availableSettingsLanguages().contains(SettingsAppLanguage.Other),
        )
        assertFalse(
            options.availableSettingsLanguages().contains(SettingsAppLanguage.English),
        )
    }

    @Test
    fun hamrahPay_coercesOtherAndEnglishToDari() {
        val options = optionsFor(ActivePsp.HP)

        assertEquals(AppLanguage.PersianDari, options.coerceCoreLanguage(AppLanguage.Other))
        assertEquals(AppLanguage.PersianDari, options.coerceCoreLanguage(AppLanguage.English))
    }

    private fun optionsFor(activePsp: ActivePsp): PspSettingsLanguageOptions {
        return PspSettingsLanguageOptions(
            AppRuntimeConfig(
                activePsp = activePsp,
                activeProtocol = ActiveProtocol.ISO,
                activeDevice = ActiveDevice.K9,
                enabledFeatures = emptySet(),
            ),
        )
    }
}
