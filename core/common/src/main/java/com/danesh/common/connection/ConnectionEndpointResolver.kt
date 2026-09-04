package com.danesh.common.connection

import com.danesh.common.network.NetworkConnectionType
import com.danesh.common.network.NetworkConnectivityMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionEndpointResolver @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
) {

    fun activeNetworkType(): NetworkConnectionType =
        networkConnectivityMonitor.connectionType.value

    fun isDualChannelConfigured(): Boolean =
        connectionPreferences.isWifiServerConfigured() &&
            connectionPreferences.isGprsServerConfigured()

    fun primaryChannelForTransaction(): ConnectionChannel =
        when (activeNetworkType()) {
            NetworkConnectionType.WIFI -> ConnectionChannel.WIFI
            NetworkConnectionType.GPRS -> ConnectionChannel.GPRS
            NetworkConnectionType.NONE -> connectionPreferences.getMainServerConnectionType()
        }

    fun backupChannelForTransaction(): ConnectionChannel = when (primaryChannelForTransaction()) {
        ConnectionChannel.WIFI -> ConnectionChannel.GPRS
        ConnectionChannel.GPRS -> ConnectionChannel.WIFI
    }

    fun alternateBackupChannelForUi(): ConnectionChannel? {
        if (!isDualChannelConfigured()) {
            return null
        }
        return backupChannelForTransaction()
    }

    fun resolveForTransaction(): List<ConnectionEndpoint> {
        val primaryChannel = primaryChannelForTransaction()
        val backupChannel = backupChannelForTransaction()

        val endpoints = mutableListOf<ConnectionEndpoint>()
        endpointFor(primaryChannel)?.let { endpoints += it }
        if (isBackupEnabledFor(backupChannel)) {
            endpointFor(backupChannel)?.let { backup ->
                if (endpoints.none { it.ip == backup.ip && it.port == backup.port }) {
                    endpoints += backup
                }
            }
        }
        return endpoints
    }

    fun isBackupEnabledFor(channel: ConnectionChannel): Boolean = when (channel) {
        ConnectionChannel.GPRS -> connectionPreferences.isGprsBackupEnabled()
        ConnectionChannel.WIFI -> connectionPreferences.isWifiBackupEnabled()
    }

    fun setBackupEnabledFor(channel: ConnectionChannel, enabled: Boolean) {
        when (channel) {
            ConnectionChannel.GPRS -> connectionPreferences.setGprsBackupEnabled(enabled)
            ConnectionChannel.WIFI -> connectionPreferences.setWifiBackupEnabled(enabled)
        }
    }

    private fun endpointFor(channel: ConnectionChannel): ConnectionEndpoint? {
        val ip = when (channel) {
            ConnectionChannel.WIFI -> connectionPreferences.getWifiIp()
            ConnectionChannel.GPRS -> connectionPreferences.getGprsIp()
        }
        val port = when (channel) {
            ConnectionChannel.WIFI -> connectionPreferences.getWifiPort()
            ConnectionChannel.GPRS -> connectionPreferences.getGprsPort()
        }
        if (ConnectionAddressValidator.isMainServerConfigured(ip, port)) {
            return ConnectionEndpoint(channel = channel, ip = ip.trim(), port = port)
        }
        return null
    }
}
