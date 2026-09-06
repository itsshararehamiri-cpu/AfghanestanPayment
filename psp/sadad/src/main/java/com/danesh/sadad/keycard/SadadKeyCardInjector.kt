package com.danesh.sadad.keycard

import com.danesh.api.DeviceConfigurationStore
import com.danesh.core.Device
import com.danesh.core.SensitiveBytes
import javax.inject.Inject
import javax.inject.Singleton

data class SadadKeyCardMasterKeys(
    val terminalMasterKey: ByteArray,
    val macKey: ByteArray,
    val dataKey: ByteArray,
    val pinKey: ByteArray,
    val initMacKey: ByteArray,
    val initDataKey: ByteArray,
)

data class SadadKeyCardKcvSummary(
    val terminalMasterKey: String,
    val mac: String,
    val data: String,
    val pin: String,
)

/**
 * تزریق کلیدهای رمزگشایی‌شده از کارت B/C به PED.
 *
 * نگاشت شماره کلید مستند به اسلات دستگاه (به دلیل نبود مستندسازی رسمی سداد برای این بخش،
 * بر اساس نام‌گذاری کلیدها و مدل موجود Device انتخاب شده و باید در میدان تایید شود):
 * - TerminalMasterKey (01) → TMK (Device.INDEX_TMK)
 * - MAC (02)              → کلید کاری MAC (Device.INDEX_MAC)
 * - DATA (03)             → کلید کاری DATA
 * - Init Pin (04)         → کلید کاری PIN (تنها کاندید موجود برای رمزگذاری PIN block)
 * - Init Mac (05) و Init Data (06) فعلاً به هیچ اسلاتی تزریق نمی‌شوند چون در این کدبیس
 *   مصرف‌کننده‌ی شبکه‌ای مشخصی ندارند؛ فقط برای بازرسی/KCV در دسترس قرار می‌گیرند.
 *
 * نوشتن هر سه‌ی MAC/DATA/PIN از مسیر plaintext-write دستگاه (که خودش کلید را زیر TMK
 * فعال رمز و تزریق می‌کند) انجام می‌شود، نه از مسیر LoadTmkEncrypted*.
 */
@Singleton
class SadadKeyCardInjector @Inject constructor(
    private val device: Device,
    private val configurationStore: DeviceConfigurationStore,
) {
    suspend fun inject(keys: SadadKeyCardMasterKeys): SadadKeyCardKcvSummary {
        try {
            device.writeMasterKey(keys.terminalMasterKey, index = device.INDEX_TMK)
            device.writeMacKey(keys.macKey, index = device.INDEX_MAC)
            device.writeDataKey(keys.dataKey)
            device.writePinKey(keys.pinKey)
            configurationStore.markConfigured()

            val pedKcv = runCatching { device.getKCv() }.getOrNull()
            return SadadKeyCardKcvSummary(
                terminalMasterKey = pedKcv?.master?.takeIf { it.isNotBlank() }
                    ?: SadadKeyCardCrypto.kcvHex(keys.terminalMasterKey),
                mac = pedKcv?.mac?.takeIf { it.isNotBlank() }
                    ?: SadadKeyCardCrypto.kcvHex(keys.macKey),
                data = pedKcv?.data?.takeIf { it.isNotBlank() }
                    ?: SadadKeyCardCrypto.kcvHex(keys.dataKey),
                pin = pedKcv?.pin?.takeIf { it.isNotBlank() }
                    ?: SadadKeyCardCrypto.kcvHex(keys.pinKey),
            )
        } finally {
            SensitiveBytes.wipe(keys.terminalMasterKey)
            SensitiveBytes.wipe(keys.macKey)
            SensitiveBytes.wipe(keys.dataKey)
            SensitiveBytes.wipe(keys.pinKey)
            SensitiveBytes.wipe(keys.initMacKey)
            SensitiveBytes.wipe(keys.initDataKey)
        }
    }
}
