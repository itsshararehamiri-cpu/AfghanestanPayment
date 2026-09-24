package com.danesh.settings.data

import android.content.Context
import com.danesh.settings.config.DefaultMerchantPasswordProvider
import com.danesh.settings.config.MerchantPasswordLockPolicy
import com.danesh.settings.model.MerchantPasswordCheck
import com.danesh.settings.domain.SupportAccessPassword
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPasswordRepository @Inject constructor(
    @ApplicationContext context: Context,
    defaultPasswordProvider: DefaultMerchantPasswordProvider,
    private val lockPolicy: MerchantPasswordLockPolicy,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** سداد: 0000 — همراه‌پی و به‌پرداخت: 1111 (بر اساس PSP فعال). */
    val defaultMerchantPassword: String = defaultPasswordProvider.defaultPassword()

    fun getMerchantPassword(): String =
        prefs.getString(KEY_MERCHANT_PASSWORD, defaultMerchantPassword)
            ?: defaultMerchantPassword

    fun validateMerchantPassword(password: String): Boolean =
        checkMerchantPassword(password) == MerchantPasswordCheck.VALID

    /**
     * بررسی رمز پذیرنده با شمارش ورودهای اشتباه (شمارنده در prefs می‌ماند و با بستن برنامه صفر نمی‌شود).
     * وقتی قفل است حتی رمز درست هم پذیرفته نمی‌شود تا پشتیبانی رمز را بازنشانی کند.
     */
    @Synchronized
    fun checkMerchantPassword(password: String): MerchantPasswordCheck {
        if (isMerchantPasswordLocked()) return MerchantPasswordCheck.LOCKED
        if (password == getMerchantPassword()) {
            prefs.edit().putInt(KEY_MERCHANT_FAILED_ATTEMPTS, 0).apply()
            return MerchantPasswordCheck.VALID
        }
        val maxAttempts = lockPolicy.maxFailedAttempts() ?: return MerchantPasswordCheck.WRONG
        val failed = prefs.getInt(KEY_MERCHANT_FAILED_ATTEMPTS, 0) + 1
        val locked = failed >= maxAttempts
        prefs.edit()
            .putInt(KEY_MERCHANT_FAILED_ATTEMPTS, failed)
            .putBoolean(KEY_MERCHANT_PASSWORD_LOCKED, locked)
            .apply()
        return if (locked) MerchantPasswordCheck.LOCKED else MerchantPasswordCheck.WRONG
    }

    fun isMerchantPasswordLocked(): Boolean =
        lockPolicy.maxFailedAttempts() != null &&
            prefs.getBoolean(KEY_MERCHANT_PASSWORD_LOCKED, false)

    fun updateMerchantPassword(newPassword: String) {
        prefs.edit()
            .putString(KEY_MERCHANT_PASSWORD, newPassword)
            .putBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, false)
            .putInt(KEY_MERCHANT_FAILED_ATTEMPTS, 0)
            .putBoolean(KEY_MERCHANT_PASSWORD_LOCKED, false)
            .apply()
    }

    /** بازنشانی از تنظیمات پشتیبانی — قفل رمز پذیرنده را هم باز می‌کند. */
    fun resetMerchantPassword() {
        prefs.edit()
            .putString(KEY_MERCHANT_PASSWORD, defaultMerchantPassword)
            .putBoolean(KEY_MUST_CHANGE_MERCHANT_PASSWORD, true)
            .putInt(KEY_MERCHANT_FAILED_ATTEMPTS, 0)
            .putBoolean(KEY_MERCHANT_PASSWORD_LOCKED, false)
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
        private const val KEY_MERCHANT_FAILED_ATTEMPTS = "merchant_password_failed_attempts"
        private const val KEY_MERCHANT_PASSWORD_LOCKED = "merchant_password_locked"
    }
}
