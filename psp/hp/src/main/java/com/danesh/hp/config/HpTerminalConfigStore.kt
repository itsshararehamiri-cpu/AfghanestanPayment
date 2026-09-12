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
        clearPendingRequest()
    }

    /**
     * درخواست 1304/1305 در حال انتظارِ پاسخ — بخش 9.2 مستند KAREN: «تلاش اول از 1304
     * استفاده می‌کند؛ تلاش‌های بعدی از 1305 استفاده می‌کنند در حالی که STAN، timestamp
     * محلی، و DE72 کسب‌وکاری را حفظ می‌کنند». تا وقتی پاسخ 1314 معتبر (DE39=300) دریافت
     * و فعال نشده، همین مقادیر باید بدون تغییر در تلاش بعدی با MTI=1305 بازفرستاده شوند.
     */
    fun pendingRequest(): PendingConfigRequest? {
        val stan = prefs.getString(KEY_PENDING_STAN, null) ?: return null
        val dateTime = prefs.getString(KEY_PENDING_DATE_TIME, null) ?: return null
        val transmissionDateTime = prefs.getString(KEY_PENDING_TRANSMISSION_DATE_TIME, null) ?: return null
        val f72 = prefs.getString(KEY_PENDING_F72, null) ?: return null
        return PendingConfigRequest(
            stan = stan,
            dateTime = dateTime,
            transmissionDateTime = transmissionDateTime,
            f72 = f72,
        )
    }

    fun savePendingRequest(request: PendingConfigRequest) {
        prefs.edit()
            .putString(KEY_PENDING_STAN, request.stan)
            .putString(KEY_PENDING_DATE_TIME, request.dateTime)
            .putString(KEY_PENDING_TRANSMISSION_DATE_TIME, request.transmissionDateTime)
            .putString(KEY_PENDING_F72, request.f72)
            .apply()
    }

    /** بعد از پذیرش موفق 1314 یا شروع صریح یک همگام‌سازی تازه فراخوانی شود. */
    fun clearPendingRequest() {
        prefs.edit()
            .remove(KEY_PENDING_STAN)
            .remove(KEY_PENDING_DATE_TIME)
            .remove(KEY_PENDING_TRANSMISSION_DATE_TIME)
            .remove(KEY_PENDING_F72)
            .apply()
    }

    data class PendingConfigRequest(
        val stan: String,
        val dateTime: String,
        val transmissionDateTime: String,
        val f72: String,
    )

    companion object {
        private const val PREFS_NAME = "hp_terminal_config_prefs"
        private const val KEY_VERSION = "hp_active_config_version"
        private const val DEFAULT_VERSION = 0

        private const val KEY_PENDING_STAN = "hp_pending_config_stan"
        private const val KEY_PENDING_DATE_TIME = "hp_pending_config_date_time"
        private const val KEY_PENDING_TRANSMISSION_DATE_TIME = "hp_pending_config_transmission_date_time"
        private const val KEY_PENDING_F72 = "hp_pending_config_f72"
    }
}
