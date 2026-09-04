package com.danesh.common.connection

interface ConnectionPreferences {
    fun getIp(): String
    fun getPort(): Int
    fun getNii(): String
    fun saveIp(ip: String)
    fun savePort(port: Int)
    fun saveTMSIp(ip: String)
    fun saveTMSPort(port: Int)
    fun saveNii(nii: String)

    fun isDefaultDepositIdEnabled(): Boolean
    fun setDefaultDepositIdEnabled(enabled: Boolean)
    fun resetToDefaults()

    fun getTMSIp(): String
    fun getTMSPort(): Int

    fun getWifiIp(): String
    fun getWifiPort(): Int
    fun saveWifiIp(ip: String)
    fun saveWifiPort(port: Int)

    fun getGprsIp(): String
    fun getGprsPort(): Int
    fun saveGprsIp(ip: String)
    fun saveGprsPort(port: Int)

    fun isWifiServerConfigured(): Boolean
    fun isGprsServerConfigured(): Boolean

    fun isGprsBackupEnabled(): Boolean
    fun setGprsBackupEnabled(enabled: Boolean)
    fun isWifiBackupEnabled(): Boolean
    fun setWifiBackupEnabled(enabled: Boolean)

    fun getMainServerConnectionType(): ConnectionChannel
    fun setMainServerConnectionType(type: ConnectionChannel)

    fun getIdleTimeMinutes(): Int
    fun setIdleTimeMinutes(minutes: Int)

    fun getSelectedWifiSsid(): String
    fun setSelectedWifiSsid(ssid: String)

    fun isTmsServerConfigured(): Boolean
}
