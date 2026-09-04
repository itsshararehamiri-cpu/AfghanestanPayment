package com.danesh.settings.network

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class AndroidWifiNetworkScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: WifiScanPermissionChecker,
) : WifiNetworkScanner {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    override suspend fun scanNetworks(): WifiScanResult {
        permissionChecker.scanBlockedReason()?.let { reason ->
            return WifiScanResult.Failure(reason)
        }

        val manager = wifiManager ?: return WifiScanResult.Failure(WifiScanResult.Error.WIFI_UNAVAILABLE)

        if (!ensureWifiEnabled(manager)) {
            return WifiScanResult.Failure(WifiScanResult.Error.WIFI_UNAVAILABLE)
        }

        var networks = scanOnce(manager)
        if (networks.isEmpty()) {
            delay(RETRY_DELAY_MS)
            networks = readScanResults(manager)
        }

        if (networks.isEmpty() &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            !permissionChecker.isLocationEnabled()
        ) {
            return WifiScanResult.Failure(WifiScanResult.Error.LOCATION_DISABLED)
        }

        return WifiScanResult.Success(networks)
    }

    private suspend fun scanOnce(manager: WifiManager): List<WifiNetworkItem> =
        withTimeoutOrNull(SCAN_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        runCatching {
                            context?.unregisterReceiver(this)
                        }
                        if (continuation.isActive) {
                            continuation.resume(readScanResults(manager))
                        }
                    }
                }
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
                continuation.invokeOnCancellation {
                    runCatching { context.unregisterReceiver(receiver) }
                }
                @SuppressLint("MissingPermission")
                val scanStarted = manager.startScan()
                if (!scanStarted && continuation.isActive) {
                    runCatching { context.unregisterReceiver(receiver) }
                    continuation.resume(readScanResults(manager))
                }
            }
        } ?: readScanResults(manager)

    @SuppressLint("MissingPermission")
    private fun ensureWifiEnabled(manager: WifiManager): Boolean {
        if (manager.isWifiEnabled) {
            return true
        }
        return runCatching {
            @Suppress("DEPRECATION")
            manager.isWifiEnabled = true
            manager.isWifiEnabled
        }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    private fun readScanResults(manager: WifiManager): List<WifiNetworkItem> =
        manager.scanResults
            .orEmpty()
            .mapNotNull { result ->
                val ssid = result.readSsid() ?: return@mapNotNull null
                WifiNetworkItem(
                    ssid = ssid,
                    signalLevel = WifiManager.calculateSignalLevel(result.level, 4),
                )
            }
            .distinctBy { it.ssid }
            .sortedByDescending { it.signalLevel }

    private fun ScanResult.readSsid(): String? {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiSsid?.toString()?.trim()?.trim('"').orEmpty()
        } else {
            @Suppress("DEPRECATION")
            SSID?.trim().orEmpty()
        }
        return raw.takeIf { value ->
            value.isNotEmpty() && !value.equals("<unknown ssid>", ignoreCase = true)
        }
    }

    companion object {
        private const val SCAN_TIMEOUT_MS = 8_000L
        private const val RETRY_DELAY_MS = 1_500L
    }
}
