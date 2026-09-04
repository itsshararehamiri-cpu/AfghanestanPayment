package com.danesh.settings.network

sealed interface WifiScanResult {
    data class Success(val networks: List<WifiNetworkItem>) : WifiScanResult

    data class Failure(val error: Error) : WifiScanResult

    enum class Error {
        PERMISSION_DENIED,
        LOCATION_DISABLED,
        WIFI_UNAVAILABLE,
    }
}
