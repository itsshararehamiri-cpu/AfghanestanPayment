package com.danesh.bp.device

import com.danesh.api.InitInput
import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceWorkflow
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.bp.key.BpKeyMaterial
import com.danesh.core.Device
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BpDeviceWorkflow @Inject constructor(
    private val device: Device,
) : PspDeviceWorkflow {

    override fun initPreparationSteps(input: InitInput): List<PspKeyLoadStep> =
        bootstrapInitialKeySteps()

    override fun initCompletionSteps(
        request: InitRequest,
        terminalKey: ByteArray,
    ): List<PspKeyLoadStep> =
        listOf(
            PspKeyLoadStep.WriteMasterKey(
                key = terminalKey,
                index = device.INDEX_TMK,
            ),
        )

    override fun logonPreparationSteps(): List<PspKeyLoadStep> =
        bootstrapInitialKeySteps()

    private fun bootstrapInitialKeySteps(): List<PspKeyLoadStep> {
        val initialMaster = BpKeyMaterial.initialMasterKeyBytes()
        val initialMac = BpKeyMaterial.initialMacKeyBytes()
        return listOf(
            PspKeyLoadStep.WriteMasterKey(
                key = initialMaster,
                index = device.INDEX_BOOTSTRAP_TMK,
            ),
            PspKeyLoadStep.WriteMacKey(
                key = initialMac,
                index = device.INDEX_BOOTSTRAP_MAC,
            ),
        )
    }

    override fun logonCompletionSteps(workingKeys: PspLogonWorkingKeys): List<PspKeyLoadStep> =
        listOf(
            PspKeyLoadStep.LoadTmkEncryptedMacKey(
                encryptedKey = workingKeys.encryptedMacKey,
                index = device.INDEX_MAC,
            ),
            PspKeyLoadStep.LoadTmkEncryptedPinKey(
                encryptedKey = workingKeys.encryptedPinKey,
            ),
            PspKeyLoadStep.LoadTmkEncryptedDataKey(
                encryptedKey = workingKeys.encryptedDataKey,
            ),
        )
}
