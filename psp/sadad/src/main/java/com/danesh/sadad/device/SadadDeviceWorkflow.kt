package com.danesh.sadad.device

import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceWorkflow
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.core.Device
import com.danesh.sadad.key.SadadKeyMaterial
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadDeviceWorkflow @Inject constructor(
    private val device: Device,
) : PspDeviceWorkflow {

    override fun initCompletionSteps(
        request: InitRequest,
        terminalKey: ByteArray,
    ): List<PspKeyLoadStep> {
        val tmkIndex = device.INDEX_TMK + 1
        return listOf(
            PspKeyLoadStep.WriteMasterKey(
                key = terminalKey,
                index = tmkIndex,
            ),
        )
    }

    override fun logonCompletionSteps(workingKeys: PspLogonWorkingKeys): List<PspKeyLoadStep> =
        emptyList()

    fun hardcodedWorkingKeys(): PspLogonWorkingKeys =
        PspLogonWorkingKeys(
            encryptedMacKey = SadadKeyMaterial.encryptedMacKeyBytes(),
            encryptedPinKey = SadadKeyMaterial.encryptedPinKeyBytes(),
            encryptedDataKey = ByteArray(0),
        )
}
