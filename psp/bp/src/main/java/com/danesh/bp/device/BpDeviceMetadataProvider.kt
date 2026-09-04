package com.danesh.bp.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.danesh.api.PspDeviceMetadata
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.TransactionContextProvider
import com.danesh.common.app.AppVersionProvider
import com.danesh.core.Device
import com.danesh.core.DeviceDefaults
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpDeviceMetadataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val device: Device,
    private val appVersionProvider: AppVersionProvider,
    private val contextProvider: TransactionContextProvider,
) : PspDeviceMetadataProvider {

    override suspend fun metadata(): PspDeviceMetadata {
        return PspDeviceMetadata(
            serial = deviceSerial(),
            appVersion = normalizeAppVersion(appVersionProvider.versionName()),
            imei = readImei(),
            simSerial = readSimSerial().orEmpty(),
        )
    }

    /**
     * F63 AppVersion — حداقل سه بخشی (مثلاً 1.0 → 1.0.0).
     */
    private fun normalizeAppVersion(raw: String): String {
        val cleaned = raw.trim().ifBlank { "1.0.0" }
        val parts = cleaned.split('.').map { it.trim() }.filter { it.isNotEmpty() }
        return when (parts.size) {
            0 -> "1.0.0"
            1 -> "${parts[0]}.0.0"
            2 -> "${parts[0]}.${parts[1]}.0"
            else -> parts.take(3).joinToString(".")
        }
    }

    override suspend fun deviceSerial(): String {
        // سریال ثبت‌شده روی سوئیچ BP (مثلاً D1V2890000001) اولویت دارد؛
        // PoR = SHA256(Ticket_1 + Serial) باید با همان سریال ثبت‌شده ساخته شود.
        val configured = contextProvider.getTerminalConfig().deviceSerial.trim()
        if (configured.isNotEmpty()) return configured

        val hardware = runCatching { device.getSerial() }.getOrDefault("").trim()
        if (hardware.isNotEmpty()) return hardware

        return DeviceDefaults.SERIAL
    }

    private suspend fun readImei(): String {
        val fromDevice = runCatching { device.getImei() }.getOrDefault("").trim()
        val normalized = normalizeSingleImei(fromDevice)
        if (normalized.isNotEmpty()) return normalized
        return normalizeSingleImei(readTelephonyImei().orEmpty())
    }

    /**
     * مستند F63: یک IMEI.
     * SDK گاهی Dual-SIM را به شکل `imei1-imei2` می‌دهد — فقط اولی را می‌فرستیم.
     */
    private fun normalizeSingleImei(raw: String): String {
        val cleaned = raw.trim()
        if (cleaned.isEmpty()) return ""
        val first = cleaned
            .split('-', ',', '/', ';', ' ', '\t')
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
        return first.filter { it.isDigit() }.ifEmpty { first }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun readTelephonyImei(): String? {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return null
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephony.imei
            } else {
                @Suppress("DEPRECATION")
                telephony.deviceId
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun readSimSerial(): String? {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return null
        return runCatching {
            telephony.simSerialNumber
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
