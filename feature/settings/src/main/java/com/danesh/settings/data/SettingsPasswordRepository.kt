package com.danesh.settings.data

import android.content.Context
import com.danesh.settings.config.DefaultMerchantPasswordProvider
import com.danesh.settings.domain.SupportAccessPassword
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPasswordRepository @Inject constructor(
    @ApplicationContext context: Context,
    defaultPasswordProvider: DefaultMerchantPasswordProvider,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** سداد: 0000 — همراه‌پی و به‌پرداخت: 1111 (بر اساس PSP فعال). */
    val defaultMerchantPassword: String = defaultPasswordProvider.defaultPassword()

    fun getMerchantPassword(): String =
        prefs.getString(KEY_MERCHANT_PASSWORD, defaultMerchantPassword)
            ?: defaultMerchantPassword

    fun validateMerchantPassword(password: String): Boolean =
        password == getMerchantPassword()

    fun updateMerchantPassword(newPassword: String) {
        prefs.edit()
            .putString(KEY_MERCHANT_PASSWORD, newPassword)
            .putBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, false)
            .apply()
    }

    fun resetMerchantPassword() {
        prefs.edit()
            .putString(KEY_MERCHANT_PASSWORD, defaultMerchantPassword)
            .putBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, true)
            .apply()
    }

    fun resetAccessSettings() {
        prefs.edit().clear().apply()
        resetMerchantPassword()
    }

    fun mustChangeMerchantPassword(): Boolean =
        prefs.getBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, false)

    fun requiresMerchantPasswordChange(): Boolean =
        mustChangeMerchantPassword() || getMerchantPassword() == defaultMerchantPassword

    fun clearMustChangeMerchantPassword() {
        prefs.edit().putBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, false).apply()
    }

    fun validateSupportPassword(password: String): Boolean =
        SupportAccessPassword.matches(password)

    companion object {
        private const val PREFS_NAME = "settings_access_prefs"
        private const val KEY_MERCHANT_PASSWORD = "merchant_password"
        private const val KEY_MUST_CHANGE_MERCHANT_PASSWORD = "must_change_merchant_password"
    }
}
