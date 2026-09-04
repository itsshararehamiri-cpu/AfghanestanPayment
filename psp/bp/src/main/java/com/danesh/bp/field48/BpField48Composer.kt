package com.danesh.bp.field48

import com.danesh.common.connection.ConnectionPreferences
import com.danesh.core.Device
import com.danesh.iso.IsoMessage
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpField48Composer @Inject constructor(
    private val connectionPreferences: ConnectionPreferences,
    private val device: Device,
) {

    suspend fun applyTerminalNetworkTags(
        message: IsoMessage,
        communicationType: String = DEFAULT_COMMUNICATION,
    ) {
        val modelRaw = runCatching { device.getModel() }.getOrDefault("").trim()
        val brand = resolveBrand(modelRaw)
        // مدل باید کد مدل باشد (مثلاً S910 / K9)، نه تکرار Brand
        val posModel = resolveModel(modelRaw)
        with(message) {
            setField48 {
                setField48Tag(BpField48Tags.SWITCH_IP, connectionPreferences.getIp().trim())
                setField48Tag(BpField48Tags.TERMINAL_IP, DEFAULT_TERMINAL_IP)
                setField48Tag(BpField48Tags.SWITCH_PORT, connectionPreferences.getPort().toString())
                setField48Tag(BpField48Tags.POS_BRAND, brand)
                setField48Tag(BpField48Tags.COMMUNICATION_TYPE, communicationType)
                setField48Tag(BpField48Tags.POS_MODEL, posModel)
            }
        }
    }

    fun applyReceiptPrintedFlag(message: IsoMessage, printed: String = "1") {
        with(message) {
            setField48 {
                setField48Tag(BpField48Tags.LAST_SUCCESS_STAN, printed)
            }
        }
    }

    fun applyTags(message: IsoMessage, tags: Map<String, String>) {
        if (tags.isEmpty()) return
        with(message) {
            setField48 {
                tags.forEach { (tag, value) ->
                    val cleaned = value.trim()
                    if (cleaned.isNotEmpty()) {
                        setField48Tag(tag, cleaned)
                    }
                }
            }
        }
    }

    private fun resolveBrand(model: String): String {
        val normalized = model.trim().uppercase()
        return when {
            normalized.contains("PAX") -> "PAX"
            // برند رسمی دستگاه‌های K9/K10 در به‌پرداخت
            normalized.contains("K9") || normalized.contains("K10") -> BRAND_CENTERM
            normalized.contains("UROVO") -> "UROVO"
            normalized.contains("CASTLES") -> "CASTLES"
            else -> normalized.take(8).ifBlank { BRAND_CENTERM }
        }
    }

    private fun resolveModel(model: String): String {
        val normalized = model.trim().uppercase()
        return when {
            normalized.isBlank() -> "K9"
            normalized.contains("K10") -> "K10"
            normalized.contains("K9") -> "K9"
            else -> normalized.take(8)
        }
    }

    companion object {
        const val DEFAULT_COMMUNICATION = "LAN"
        const val BRAND_CENTERM = "centerm"
        private const val DEFAULT_TERMINAL_IP = "127.0.0.1"
    }
}
