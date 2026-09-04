package com.danesh.engine

import android.util.Log
import com.danesh.api.PspKeyLoadStep
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import com.danesh.core.SensitiveBytes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PspDeviceWorkflowExecutor @Inject constructor(
    private val device: Device,
) {
    suspend fun execute(steps: List<PspKeyLoadStep>) {
        DeviceTrace.step("Executor", "started steps=${steps.size}")
        var pendingWrapTmk: ByteArray? = null
        try {
            for ((index, step) in steps.withIndex()) {
                DeviceTrace.step("Executor", "step ${index + 1}/${steps.size}: ${step.describe()}")
                when (step) {
                    is PspKeyLoadStep.WriteMasterKey -> {
                        val tmkIndex = step.index ?: device.INDEX_TMK
                        DeviceTrace.step(
                            "Executor",
                            "قبل از inject TMK index=$tmkIndex keyLen=${step.key.size}",
                        )
                        pendingWrapTmk?.let { SensitiveBytes.wipe(it) }
                        pendingWrapTmk = step.key.copyOf()
                        device.writeMasterKey(masterKey = step.key, index = tmkIndex)
                        SensitiveBytes.wipe(step.key)
                        DeviceTrace.step("Executor", "inject TMK index=$tmkIndex نتیجه=موفق")
                    }

                    is PspKeyLoadStep.WriteMacKey -> {
                        val makIndex = step.index ?: device.INDEX_MAC
                        val wrap = pendingWrapTmk
                            ?: error("WriteMacKey requires WriteMasterKey in the same execute batch")
                        DeviceTrace.step(
                            "Executor",
                            "قبل از inject MAK index=$makIndex keyLen=${step.key.size}",
                        )
                        device.writeMacKey(macKey = step.key, index = makIndex, wrappingTmk = wrap)
                        SensitiveBytes.wipe(step.key)
                        pendingWrapTmk?.let { SensitiveBytes.wipe(it) }
                        pendingWrapTmk = null
                        DeviceTrace.step("Executor", "inject MAK index=$makIndex نتیجه=موفق")
                    }

                    is PspKeyLoadStep.WritePinKey -> {
                        error("WritePinKey is not supported — use LoadTmkEncryptedPinKey (PED-only)")
                    }

                    is PspKeyLoadStep.WriteDataKey -> {
                        error("WriteDataKey is not supported — use LoadTmkEncryptedDataKey (PED-only)")
                    }

                    is PspKeyLoadStep.LoadTmkEncryptedMacKey -> {
                        val makIndex = step.index ?: device.INDEX_MAC
                        DeviceTrace.step(
                            "Executor",
                            "قبل از inject MAK index=$makIndex encryptedLen=${step.encryptedKey.size}",
                        )
                        device.loadTmkEncryptedMacKey(
                            encryptedKey = step.encryptedKey,
                            index = makIndex,
                        )
                        SensitiveBytes.wipe(step.encryptedKey)
                        DeviceTrace.step("Executor", "inject MAK index=$makIndex نتیجه=موفق")
                    }

                    is PspKeyLoadStep.LoadTmkEncryptedPinKey -> {
                        DeviceTrace.step(
                            "Executor",
                            "قبل از inject PIK index=${device.INDEX_PIN} " +
                                "encryptedLen=${step.encryptedKey.size}",
                        )
                        device.loadTmkEncryptedPinKey(step.encryptedKey)
                        SensitiveBytes.wipe(step.encryptedKey)
                        DeviceTrace.step("Executor", "inject PIK index=${device.INDEX_PIN} نتیجه=موفق")
                    }

                    is PspKeyLoadStep.LoadTmkEncryptedDataKey -> {
                        DeviceTrace.step(
                            "Executor",
                            "قبل از inject DEK index=${device.INDEX_DATA} encryptedLen=${step.encryptedKey.size}",
                        )
                        device.loadTmkEncryptedDataKey(step.encryptedKey)
                        SensitiveBytes.wipe(step.encryptedKey)
                        DeviceTrace.step("Executor", "inject DEK index=${device.INDEX_DATA} نتیجه=موفق")
                    }
                }
                DeviceTrace.step("Executor", "step ${index + 1}/${steps.size} completed")
            }
        } finally {
            pendingWrapTmk?.let { SensitiveBytes.wipe(it) }
        }
        device.getCheckValue("gggggg")
        DeviceTrace.step("Executor", "all steps completed")
    }

    private fun PspKeyLoadStep.describe(): String = when (this) {
        is PspKeyLoadStep.WriteMasterKey -> "WriteMasterKey index=$index"
        is PspKeyLoadStep.WriteMacKey -> "WriteMacKey index=$index"
        is PspKeyLoadStep.WritePinKey -> "WritePinKey"
        is PspKeyLoadStep.WriteDataKey -> "WriteDataKey"
        is PspKeyLoadStep.LoadTmkEncryptedMacKey -> "LoadTmkEncryptedMacKey index=$index"
        is PspKeyLoadStep.LoadTmkEncryptedPinKey -> "LoadTmkEncryptedPinKey"
        is PspKeyLoadStep.LoadTmkEncryptedDataKey -> "LoadTmkEncryptedDataKey"
    }
}
