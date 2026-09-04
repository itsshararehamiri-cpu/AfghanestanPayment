package com.danesh.settings.network

data class WifiNetworkItem(
    val ssid: String,
    val signalLevel: Int,
)

interface WifiNetworkScanner {
    suspend fun scanNetworks(): WifiScanResult
}
