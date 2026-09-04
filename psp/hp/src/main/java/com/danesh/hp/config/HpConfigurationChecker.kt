package com.danesh.hp.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.TransactionContextProvider
import com.danesh.common.connection.ConnectionPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * همراه‌پی: پیکربندی = اتصال شبکه + (Init موفق یا شناسه ترمینال/پذیرنده).
 */
@Singleton
class HpConfigurationChecker @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val connectionPreferences: ConnectionPreferences,
    private val configurationStore: DeviceConfigurationStore,
) : PspConfigurationChecker {

    override fun isConfigured(): Boolean {
        if (!hasConnectionSettings()) return false
        if (configurationStore.isConfigured()) return true

        val config = contextProvider.getTerminalConfig()
        return config.terminalId.isNotBlank() && config.merchantId.isNotBlank()
    }

    private fun hasConnectionSettings(): Boolean =
        connectionPreferences.getIp().isNotBlank() && connectionPreferences.getPort() > 0
}
