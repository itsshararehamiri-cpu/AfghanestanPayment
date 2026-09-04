package com.danesh.hp.device

import com.danesh.api.BallotType
import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceWorkflow
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.core.Device
import com.danesh.hp.key.HpKeyMaterial
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ترتیب inject کلید برای PSP همراه‌پی.
 *
 * - Init: نوشتن TMK روی INDEX_TMK
 * - Logon: reload TMK + inject MAK/PIK رمزشده زیر TMK
 */
@Singleton
class HpDeviceWorkflow @Inject constructor(
    private val device: Device,
) : PspDeviceWorkflow {

    override fun initCompletionSteps(
        request: InitRequest,
        terminalKey: ByteArray,
    ): List<PspKeyLoadStep> {
        val tmkIndex =
            device.INDEX_TMK + 1

        return listOf(
            PspKeyLoadStep.WriteMasterKey(
                key = HpKeyMaterial.masterKeyBytes(),
                index = tmkIndex,
            ),
        )
    }

    override fun logonCompletionSteps(workingKeys: PspLogonWorkingKeys): List<PspKeyLoadStep> {

        return listOf( PspKeyLoadStep.WriteMasterKey(
            key = HpKeyMaterial.masterKeyBytes()
        ),
            PspKeyLoadStep.LoadTmkEncryptedMacKey(
                encryptedKey = workingKeys.encryptedMacKey,
                index = device.INDEX_MAC,
            ),
            PspKeyLoadStep.LoadTmkEncryptedPinKey(
                encryptedKey = workingKeys.encryptedPinKey,
            ),
//            PspKeyLoadStep.LoadTmkEncryptedDataKey(
//                encryptedKey = workingKeys.encryptedDataKey,
//            ),


        )
    }

    fun hardcodedWorkingKeys(): PspLogonWorkingKeys {

        return PspLogonWorkingKeys(
            encryptedMacKey = HpKeyMaterial.encryptedMacKeyBytes(),
            encryptedPinKey = HpKeyMaterial.encryptedPinKeyBytes(),
            encryptedDataKey = ByteArray(0),// masterKey = HpKeyMaterial.masterKeyBytes(),
        )
    }
}
