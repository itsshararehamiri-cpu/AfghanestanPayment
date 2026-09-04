package com.danesh.api

/**
 * بررسی «پیکربندی‌شده بودن» دستگاه — منطق هر PSP جدا است.
 */
interface PspConfigurationChecker {
    fun isConfigured(): Boolean
}

interface DeviceConfigurationStore {
    fun isConfigured(): Boolean
    fun markConfigured()
    fun clearConfigured()

    fun saveConfigurationSummary(summary: DeviceConfigurationSummary)
    fun getConfigurationSummary(): DeviceConfigurationSummary?
    fun clearConfigurationSummary()
}
