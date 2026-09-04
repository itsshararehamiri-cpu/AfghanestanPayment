package com.danesh.bp.config

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.TransactionContextProvider
import com.danesh.core.Device
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpConfigurationChecker @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val configurationStore: DeviceConfigurationStore,
    private val device: Device,
) : PspConfigurationChecker {

    override fun isConfigured(): Boolean {
        val config = contextProvider.getTerminalConfig()
        val hasTerminalIdentity =
            config.terminalId.isNotBlank() && config.merchantId.isNotBlank()
        val hasWorkingKeys = device.hasWorkingMacKeyOnPed()
        val markedConfigured = configurationStore.isConfigured()
       // return hasTerminalIdentity && (hasWorkingKeys || markedConfigured)
        return hasTerminalIdentity
    }
}
