package com.danesh.sadad.keycard

import com.danesh.api.DeviceConfigurationStore
import com.danesh.common.diagnostics.StartupTraceFile
import com.danesh.core.Device
import com.danesh.core.SensitiveBytes
import com.danesh.sadad.key.SadadWorkingMacState
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
 * - TerminalMasterKey / Init MAC / PIN / DATA کارت → اسلات PED همان اندیس کارت C
 * - کلیدهای کاری LOGON (Field 48) → اسلات C+1
 *   کلید پوشش MAC کارت فقط در حافظه می‌ماند تا کلیدهای Field 48 باز شوند و MAK روی PED
 *   با Init Mac بازنویسی نشود.
 */
@Singleton
class SadadKeyCardInjector @Inject constructor(
    private val device: Device,
    private val configurationStore: DeviceConfigurationStore,
    private val wrappingKeys: SadadWrappingKeyHolder,
    private val workingMacState: SadadWorkingMacState,
) {
    suspend fun inject(
        keys: SadadKeyCardMasterKeys,
        keyIndex: Int,
        rsaKeyIndex: Int = keyIndex,
    ): SadadKeyCardKcvSummary {
        try {
            StartupTraceFile.line(
                "Inject",
                "start cardCIndex=$keyIndex rsaIndex=$rsaKeyIndex workingIndex=${keyIndex + 1}",
            )
            wrappingKeys.storeFromCard(keys)
            workingMacState.clearWorkingMac()
            workingMacState.saveKeyIndices(cardCIndex = keyIndex, rsaKeyIndex = rsaKeyIndex)
            StartupTraceFile.line("Inject", "write TMK index=$keyIndex len=${keys.terminalMasterKey.size}")
            device.writeMasterKey(keys.terminalMasterKey, index = keyIndex)
            StartupTraceFile.line("Inject", "write MAC index=$keyIndex len=${keys.initMacKey.size}")
            device.writeMacKey(keys.initMacKey, index = keyIndex)
            StartupTraceFile.line("Inject", "write DATA index=$keyIndex len=${keys.dataKey.size}")
            device.writeDataKey(keys.dataKey, index = keyIndex)
            StartupTraceFile.line("Inject", "write PIN index=$keyIndex len=${keys.pinKey.size}")
            device.writePinKey(keys.pinKey, index = keyIndex)
            configurationStore.markConfigured()

            val pedKcv = runCatching { device.getKcvAt(keyIndex) }.getOrNull()
            StartupTraceFile.line(
                "Inject",
                "KCV read index=$keyIndex master=${pedKcv?.master.orEmpty()} " +
                    "mac=${pedKcv?.mac.orEmpty()} pin=${pedKcv?.pin.orEmpty()} data=${pedKcv?.data.orEmpty()}",
            )
            return SadadKeyCardKcvSummary(
                terminalMasterKey = pedKcv?.master.orEmpty(),
                mac = pedKcv?.mac.orEmpty(),
                data = pedKcv?.data.orEmpty(),
                pin = pedKcv?.pin.orEmpty(),
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
