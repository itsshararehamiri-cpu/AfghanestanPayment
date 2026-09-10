package com.danesh.hp.config

import android.os.Build
import android.util.Log
import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.common.app.AppVersionProvider
import com.danesh.core.Device
import com.danesh.core.DeviceDefaults
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.field48.HpField48Tlv
import javax.inject.Inject
import javax.inject.Singleton

/**
 * درخواست اختیاری پیکربندی ترمینال همراه‌پی نزد سوییچ کارن (KAREN) — بخش 9.2 مستند پروتکل.
 *
 * MTI=1304، DE24 (نگاشت‌شده به [IsoMessage.nii])="305"، DE41/DE42 فقط وقتی پروفایل فعال
 * از قبل موجود است ارسال می‌شوند و DE43 هرگز ارسال نمی‌شود. DE72 شامل رکوردهای TLV
 * 001 (TCFG)، 002 (نسخهٔ Schema)، 003 (سریال دستگاه)، 004 (مدل دستگاه)، 005 (نسخهٔ
 * برنامه)، 006 (نسخهٔ پیکربندی فعال)، 007 (هش فعال‌سازی) و 008 (SYNC) است.
 */
@Singleton
class HpTerminalConfigMessageHandler @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
    private val device: Device,
    private val appVersionProvider: AppVersionProvider,
    private val configStore: HpTerminalConfigStore,
    private val configurationStore: DeviceConfigurationStore,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)

        val configVersion = configStore.activeConfigVersion()
        val serial = deviceSerial(config)
        val model = Build.MODEL.orEmpty()
        val appVersion = appVersionProvider.versionName()
        val activeHash = computeActiveHash(config, configVersion, serial, model, appVersion)

        Log.d("TAG", "build: terminalConfig hash=$activeHash version=$configVersion")

        return messageProvider.create().apply {
            mti = TransactionIsoProfile.TERMINAL_CONFIG.mti
            transmissionDateTime = "${clock.date.drop(4)}${clock.time}"
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
            nii = TransactionIsoProfile.TERMINAL_CONFIG.messageNii
                ?: error("HP Function Code (DE24) is missing for TERMINAL_CONFIG")


            if (hasActiveProfile(config)) {
                terminalId = config.terminalId
                merchantId = config.merchantId
            }

            f72 = HpField48Tlv().apply {
                addNode(TAG_RECORD_TYPE, RECORD_TYPE)
                addNode(TAG_SCHEMA_VERSION, SCHEMA_VERSION)
                addNode(TAG_DEVICE_SERIAL, serial)
                addNode(TAG_DEVICE_MODEL, model)
                addNode(TAG_APP_VERSION, appVersion)
                addNode(TAG_CONFIG_VERSION, configVersion)
                addNode(TAG_ACTIVE_HASH, activeHash)
                addNode(TAG_CAPABILITY_ACTION, CAPABILITY_ACTION)
            }.packText()
        }
    }

    private fun hasActiveProfile(config: TerminalConfig): Boolean =
        configurationStore.isConfigured() ||
            (config.terminalId.isNotBlank() && config.merchantId.isNotBlank())

    private fun deviceSerial(config: TerminalConfig): String =
        config.deviceSerial
            .ifBlank { device.getSerial() }
            .ifBlank { DeviceDefaults.SERIAL }

    /**
     * تگ 007 هرگز در محاسبهٔ خودش لحاظ نمی‌شود؛ فقط رکوردهای هویتی/پیکربندی TLV کانونیک را
     * می‌سازیم و طبق فرمول [HpTerminalConfigHash] با پیشوند DE26/DE41/DE42/DE43 هش می‌کنیم.
     */
    private fun computeActiveHash(
        config: TerminalConfig,
        configVersion: String,
        serial: String,
        model: String,
        appVersion: String,
    ): String {
        val canonicalTlv = HpField48Tlv().apply {
            addNode(TAG_RECORD_TYPE, RECORD_TYPE)
            addNode(TAG_SCHEMA_VERSION, SCHEMA_VERSION)
            addNode(TAG_DEVICE_SERIAL, serial)
            addNode(TAG_DEVICE_MODEL, model)
            addNode(TAG_APP_VERSION, appVersion)
            addNode(TAG_CONFIG_VERSION, configVersion)
            addNode(TAG_CAPABILITY_ACTION, CAPABILITY_ACTION)
        }.packText()

        return HpTerminalConfigHash.compute(
            mcc = HpKeyConfig.MERCHANT_TYPE,
            terminalId = config.terminalId,
            merchantId = config.merchantId,
            merchantNameLocation = merchantNameLocation(config),
            canonicalTlv = canonicalTlv,
        )
    }

    private fun merchantNameLocation(config: TerminalConfig): String =
        listOf(config.merchantName, config.merchantAddress)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")

    companion object {
        private const val RECORD_TYPE = "TCFG"
        private const val SCHEMA_VERSION = "001"
        private const val CAPABILITY_ACTION = "SYNC"

        private const val TAG_RECORD_TYPE = "001"
        private const val TAG_SCHEMA_VERSION = "002"
        private const val TAG_DEVICE_SERIAL = "003"
        private const val TAG_DEVICE_MODEL = "004"
        private const val TAG_APP_VERSION = "005"
        private const val TAG_CONFIG_VERSION = "006"
        private const val TAG_ACTIVE_HASH = "007"
        private const val TAG_CAPABILITY_ACTION = "008"
    }
}
