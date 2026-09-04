package com.danesh.common.connection

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsConnectionPreferences @Inject constructor(
    @ApplicationContext context: Context,
    private val defaultsProvider: ConnectionDefaultsProvider,
) : ConnectionPreferences {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getIp(): String = getWifiIp()

    override fun getPort(): Int = getWifiPort()

    override fun saveIp(ip: String) {
        saveWifiIp(ip)
    }

    override fun savePort(port: Int) {
        saveWifiPort(port)
    }

    override fun getWifiIp(): String =
        prefs.getString(KEY_WIFI_IP, null)
            ?: prefs.getString(KEY_IP, defaultsProvider.ip)
            .orEmpty()

    override fun getWifiPort(): Int =
        if (prefs.contains(KEY_WIFI_PORT)) {
            prefs.getInt(KEY_WIFI_PORT, defaultsProvider.port)
        } else {
            prefs.getInt(KEY_PORT, defaultsProvider.port)
        }

    override fun saveWifiIp(ip: String) {
        prefs.edit().putString(KEY_WIFI_IP, ip.trim()).apply()
    }

    override fun saveWifiPort(port: Int) {
        prefs.edit().putInt(KEY_WIFI_PORT, port).apply()
    }

    override fun getGprsIp(): String =
        prefs.getString(KEY_GPRS_IP, "").orEmpty()

    override fun getGprsPort(): Int =
        prefs.getInt(KEY_GPRS_PORT, defaultsProvider.port)

    override fun saveGprsIp(ip: String) {
        prefs.edit().putString(KEY_GPRS_IP, ip.trim()).apply()
    }

    override fun saveGprsPort(port: Int) {
        prefs.edit().putInt(KEY_GPRS_PORT, port).apply()
    }

    override fun isWifiServerConfigured(): Boolean =
        ConnectionAddressValidator.isMainServerConfigured(getWifiIp(), getWifiPort())

    override fun isGprsServerConfigured(): Boolean =
        ConnectionAddressValidator.isMainServerConfigured(getGprsIp(), getGprsPort())

    override fun isGprsBackupEnabled(): Boolean =
        prefs.getBoolean(KEY_GPRS_BACKUP_ENABLED, DEFAULT_BACKUP_ENABLED)

    override fun setGprsBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GPRS_BACKUP_ENABLED, enabled).apply()
    }

    override fun isWifiBackupEnabled(): Boolean =
        prefs.getBoolean(KEY_WIFI_BACKUP_ENABLED, DEFAULT_BACKUP_ENABLED)

    override fun setWifiBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_BACKUP_ENABLED, enabled).apply()
    }

    override fun getMainServerConnectionType(): ConnectionChannel {
        val stored = prefs.getString(KEY_MAIN_SERVER_CONNECTION_TYPE, null)
        return when (stored) {
            ConnectionChannel.GPRS.name -> ConnectionChannel.GPRS
            else -> ConnectionChannel.WIFI
        }
    }

    override fun setMainServerConnectionType(type: ConnectionChannel) {
        prefs.edit().putString(KEY_MAIN_SERVER_CONNECTION_TYPE, type.name).apply()
    }

    override fun getIdleTimeMinutes(): Int =
        prefs.getInt(KEY_IDLE_TIME_MINUTES, DEFAULT_IDLE_TIME_MINUTES)

    override fun setIdleTimeMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_IDLE_TIME_MINUTES, minutes.coerceAtLeast(0)).apply()
    }

    override fun getSelectedWifiSsid(): String =
        prefs.getString(KEY_SELECTED_WIFI_SSID, "").orEmpty()

    override fun setSelectedWifiSsid(ssid: String) {
        prefs.edit().putString(KEY_SELECTED_WIFI_SSID, ssid.trim()).apply()
    }

    override fun isTmsServerConfigured(): Boolean =
        ConnectionAddressValidator.isMainServerConfigured(getTMSIp(), getTMSPort())

    override fun getNii(): String =
        prefs.getString(KEY_NII, defaultsProvider.nii).orEmpty()

    override fun saveTMSIp(ip: String) {
        prefs.edit().putString(KEY_TMS_IP, ip.trim()).apply()
    }

    override fun saveTMSPort(port: Int) {
        prefs.edit().putInt(KEY_TMS_PORT, port).apply()
    }

    override fun saveNii(nii: String) {
        prefs.edit().putString(KEY_NII, nii.trim()).apply()
    }

    override fun isDefaultDepositIdEnabled(): Boolean =
        prefs.getBoolean(KEY_DEFAULT_DEPOSIT_ID_ENABLED, DEFAULT_DEPOSIT_ID_ENABLED)

    override fun setDefaultDepositIdEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEFAULT_DEPOSIT_ID_ENABLED, enabled).apply()
    }

    override fun resetToDefaults() {
        prefs.edit()
            .remove(KEY_IP)
            .remove(KEY_PORT)
            .remove(KEY_WIFI_IP)
            .remove(KEY_WIFI_PORT)
            .remove(KEY_GPRS_IP)
            .remove(KEY_GPRS_PORT)
            .remove(KEY_NII)
            .remove(KEY_DEFAULT_DEPOSIT_ID_ENABLED)
            .remove(KEY_GPRS_BACKUP_ENABLED)
            .remove(KEY_WIFI_BACKUP_ENABLED)
            .remove(KEY_TMS_IP)
            .remove(KEY_TMS_PORT)
            .remove(KEY_MAIN_SERVER_CONNECTION_TYPE)
            .remove(KEY_IDLE_TIME_MINUTES)
            .remove(KEY_SELECTED_WIFI_SSID)
            .apply()
    }

    override fun getTMSIp(): String =
        prefs.getString(KEY_TMS_IP, defaultsProvider.tmsIp).orEmpty()

    override fun getTMSPort(): Int =
        prefs.getInt(KEY_TMS_PORT, defaultsProvider.tmsPort)

    companion object {
        private const val PREFS_NAME = "connection_prefs"
        private const val KEY_IP = "server_ip"
        private const val KEY_PORT = "server_port"
        private const val KEY_WIFI_IP = "wifi_server_ip"
        private const val KEY_WIFI_PORT = "wifi_server_port"
        private const val KEY_GPRS_IP = "gprs_server_ip"
        private const val KEY_GPRS_PORT = "gprs_server_port"
        private const val KEY_GPRS_BACKUP_ENABLED = "gprs_backup_enabled"
        private const val KEY_WIFI_BACKUP_ENABLED = "wifi_backup_enabled"
        private const val DEFAULT_BACKUP_ENABLED = false
        private const val KEY_NII = "server_nii"
        private const val KEY_DEFAULT_DEPOSIT_ID_ENABLED = "default_deposit_id_enabled"
        private const val DEFAULT_DEPOSIT_ID_ENABLED = false
        private const val KEY_TMS_IP = "tms_ip"
        private const val KEY_TMS_PORT = "tms_port"
        private const val KEY_MAIN_SERVER_CONNECTION_TYPE = "main_server_connection_type"
        private const val KEY_IDLE_TIME_MINUTES = "idle_time_minutes"
        private const val KEY_SELECTED_WIFI_SSID = "selected_wifi_ssid"
        private const val DEFAULT_IDLE_TIME_MINUTES = 0
    }
}
