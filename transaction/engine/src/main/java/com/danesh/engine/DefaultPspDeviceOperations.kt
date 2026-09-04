package com.danesh.engine

import android.util.Log
import com.danesh.api.InitInput
import com.danesh.api.InitRequest
import com.danesh.api.PspDeviceOperations
import com.danesh.api.PspDeviceWorkflow
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.core.Device
import com.danesh.core.DeviceTrace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPspDeviceOperations @Inject constructor(
    private val workflow: PspDeviceWorkflow,
    private val executor: PspDeviceWorkflowExecutor,
    private val device: Device,
) : PspDeviceOperations {

    override suspend fun prepareInit(input: InitInput) {
        val steps = workflow.initPreparationSteps(input)
        executor.execute(steps)
        DeviceTrace.step("DeviceOps", "prepareInit completed")
    }

    override suspend fun completeInit(request: InitRequest, terminalKey: ByteArray) {
        // Ticket / PoR / PoA / F62 نباید لاگ یا ذخیره شوند (مستند به‌پرداخت).
        DeviceTrace.step("DeviceOps", "completeInit — تزریق کلید پایانه به PED")
        val steps = workflow.initCompletionSteps(request, terminalKey)
        executor.execute(steps)
        DeviceTrace.step("DeviceOps", "completeInit completed")
    }

    override suspend fun prepareLogon() {
        DeviceTrace.step(
            "DeviceOps",
            "prepareLogon — inject همان Initial Master/MAC قبل از Init روی bootstrap؛ " +
                    "TMK پایانه index=${device.INDEX_TMK} دست‌نخورده",
        )
        executor.execute(workflow.logonPreparationSteps())
        DeviceTrace.step("DeviceOps", "prepareLogon completed")
    }

    override suspend fun completeLogon(workingKeys: PspLogonWorkingKeys) {
        DeviceTrace.step(
            "DeviceOps",
            "completeLogon mak=${workingKeys.encryptedMacKey.size} " +
                    "pik=${workingKeys.encryptedPinKey.size} dek=${workingKeys.encryptedDataKey.size}",
        )
        Log.d("TAG", "completeLogon: kkjkkjkfbnbnbd$workingKeys")
        val steps= workflow.logonCompletionSteps(workingKeys)
        steps.forEach {
            Log.d("TAG", "completeLogon: kkjkkjkfbnbnbda$it")

        }

        executor.execute(steps)
        device.clearWorkingMacKeyCache()
        DeviceTrace.step(
            "DeviceOps",
            "completeLogon completed — working MAK software cache cleared Terminal TMK retained"
        )
    }
}
