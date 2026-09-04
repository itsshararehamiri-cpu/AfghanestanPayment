package com.danesh.engine

import android.content.Context
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.DeviceConfigurationSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsDeviceConfigurationStore @Inject constructor(
    @ApplicationContext context: Context,
) : DeviceConfigurationStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isConfigured(): Boolean =
        prefs.getBoolean(KEY_CONFIGURED, false)

    override fun markConfigured() {
        prefs.edit().putBoolean(KEY_CONFIGURED, true).apply()
    }

    override fun clearConfigured() {
        prefs.edit().putBoolean(KEY_CONFIGURED, false).apply()
    }

    override fun saveConfigurationSummary(summary: DeviceConfigurationSummary) {
        prefs.edit()
            .putString(KEY_HW_SERIAL, summary.hardwareSerial)
            .putString(KEY_TERMINAL_ID, summary.terminalId)
            .putString(KEY_APP_VERSION, summary.appVersion)
            .putString(KEY_PROGRAM_DATE, summary.programDate)
            .putString(KEY_MERCHANT_ID, summary.merchantId)
            .apply()
    }

    override fun getConfigurationSummary(): DeviceConfigurationSummary? {
        val hardwareSerial = prefs.getString(KEY_HW_SERIAL, null) ?: return null
        val terminalId = prefs.getString(KEY_TERMINAL_ID, null) ?: return null
        val appVersion = prefs.getString(KEY_APP_VERSION, null) ?: return null
        val programDate = prefs.getString(KEY_PROGRAM_DATE, null) ?: return null
        val merchantId = prefs.getString(KEY_MERCHANT_ID, null) ?: return null
        return DeviceConfigurationSummary(
            hardwareSerial = hardwareSerial,
            terminalId = terminalId,
            appVersion = appVersion,
            programDate = programDate,
            merchantId = merchantId,
        )
    }

    override fun clearConfigurationSummary() {
        prefs.edit()
            .remove(KEY_HW_SERIAL)
            .remove(KEY_TERMINAL_ID)
            .remove(KEY_APP_VERSION)
            .remove(KEY_PROGRAM_DATE)
            .remove(KEY_MERCHANT_ID)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "device_configuration_prefs"
        private const val KEY_CONFIGURED = "psp_configured"
        private const val KEY_HW_SERIAL = "config_hw_serial"
        private const val KEY_TERMINAL_ID = "config_terminal_id"
        private const val KEY_APP_VERSION = "config_app_version"
        private const val KEY_PROGRAM_DATE = "config_program_date"
        private const val KEY_MERCHANT_ID = "config_merchant_id"
    }
}
