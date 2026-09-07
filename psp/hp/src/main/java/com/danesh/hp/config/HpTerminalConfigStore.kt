package com.danesh.hp.config

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * نسخهٔ محلی پیکربندی فعال ترمینال همراه‌پی نزد سوییچ کارن (KAREN) — تگ 006 فیلد 72.
 * تا قبل از اولین همگام‌سازی موفق مقدار "0" است.
 */
@Singleton
class HpTerminalConfigStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun activeConfigVersion(): String =
        prefs.getInt(KEY_VERSION, DEFAULT_VERSION).toString()

    /** بعد از پذیرش موفق پاسخ 1314 (DE39=300) فراخوانی شود. */
    fun markActivated() {
        val next = prefs.getInt(KEY_VERSION, DEFAULT_VERSION) + 1
        prefs.edit().putInt(KEY_VERSION, next).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_VERSION).apply()
    }

    companion object {
        private const val PREFS_NAME = "hp_terminal_config_prefs"
        private const val KEY_VERSION = "hp_active_config_version"
        private const val DEFAULT_VERSION = 0
    }
}
