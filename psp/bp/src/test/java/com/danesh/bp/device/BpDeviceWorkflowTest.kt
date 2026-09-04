package com.danesh.bp.device

import com.danesh.api.BallotType
import com.danesh.api.InitInput
import com.danesh.api.InitRequest
import com.danesh.api.PspKeyLoadStep
import com.danesh.api.PspLogonWorkingKeys
import com.danesh.core.Device
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BpDeviceWorkflowTest {

    private val device = object : Device {
        override val INDEX_MAC = 1
        override val INDEX_TMK = 1
        override val INDEX_BOOTSTRAP_TMK = 2
        override val INDEX_BOOTSTRAP_MAC = 2
        override val INDEX_TEK = 1
        override val INDEX_DATA = 1
        override val INDEX_PIN = 1
        override val hasKeyboard = false
        override suspend fun getModel() = "TEST"
        override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) = Unit
        override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?) = Unit
        override suspend fun writeDataKey(dataKey: ByteArray) = Unit
        override suspend fun writePinKey(pinKey: ByteArray) = Unit
        override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) = Unit
        override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) = Unit
        override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) = Unit
        override suspend fun getMac(data: ByteArray, index: Int, keyType: com.danesh.core.MacKeyType) = ByteArray(8)
        override suspend fun readCard(context: android.content.Context, onSuccess: (String, String) -> Unit, onError: (String) -> Unit, onTimeOut: () -> Unit) = Unit
        override suspend fun getPinBlock(context: android.content.Context, pan: String, onError: (String) -> Unit, onInput: (Int) -> Unit, onConfirm: (String) -> Unit, onCancel: () -> Unit, onTimeOut: () -> Unit) = Unit
        override  fun getSerial() = "SERIAL"
        override suspend fun decryptData(data: ByteArray, onSuccess: (ByteArray) -> Unit, onError: (String) -> Unit) = Unit
        override suspend fun print(bitmap: android.graphics.Bitmap, context: android.content.Context, onSuccess: () -> Unit, onFailed: (String) -> Unit, reportErrorToUi: Boolean) = Unit
        override suspend fun getPrinterError() = ""
        override fun encrypt(data: ByteArray) = data
        override fun decrypt(data: ByteArray) = data
        override fun powerOnIcCard() = false
        override fun powerOffIcCard() = Unit
        override fun isIcCardDetect() = false
        override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit) = null
        override suspend fun setDateTime(dataTime: String) = Unit
        override suspend fun getBatteryStatus() = false
        override fun disableHome() = Unit
        override fun enableHome() = Unit
        override suspend fun scan(context: android.content.Context, onSuccess: (String) -> Unit, onError: (String) -> Unit, onTimeout: () -> Unit, onCancel: () -> Unit) = Unit
        override suspend fun getKCv() = com.danesh.core.KCV("", "", "", "")
        override suspend fun beep(context: android.content.Context, onSuccess: () -> Unit, onFailed: (String) -> Unit) = Unit
        override suspend fun ledOn(onError: (String) -> Unit) = Unit
        override suspend fun ledOff(onError: (String) -> Unit) = Unit
        override fun getCheckValue(): ByteArray {
            return ByteArray(0)
        }
    }

    private val workflow = BpDeviceWorkflow(device)

    @Test
    fun initPreparationSteps_loadsInitialMasterAndMacOnBootstrap() {
        val steps = workflow.initPreparationSteps(InitInput(BallotType.FIRST, "", ""))
        assertEquals(2, steps.size)
        assertTrue(steps[0] is PspKeyLoadStep.WriteMasterKey)
        assertTrue(steps[1] is PspKeyLoadStep.WriteMacKey)
        assertEquals(2, (steps[0] as PspKeyLoadStep.WriteMasterKey).index)
        assertEquals(2, (steps[1] as PspKeyLoadStep.WriteMacKey).index)
    }

    @Test
    fun initCompletionSteps_writesTerminalTmkOnWorkingIndex() {
        val steps = workflow.initCompletionSteps(
            request = InitRequest(BallotType.FIRST, "", ""),
            terminalKey = ByteArray(16),
        )
        assertEquals(1, steps.size)
        assertTrue(steps[0] is PspKeyLoadStep.WriteMasterKey)
        assertEquals(1, (steps[0] as PspKeyLoadStep.WriteMasterKey).index)
    }

    @Test
    fun logonPreparationSteps_injectsInitialMasterAndMacOnBootstrap() {
        val steps = workflow.logonPreparationSteps()
        assertEquals(2, steps.size)
        assertTrue(steps[0] is PspKeyLoadStep.WriteMasterKey)
        assertTrue(steps[1] is PspKeyLoadStep.WriteMacKey)
        assertEquals(2, (steps[0] as PspKeyLoadStep.WriteMasterKey).index)
        assertEquals(2, (steps[1] as PspKeyLoadStep.WriteMacKey).index)
    }

    @Test
    fun logonCompletionSteps_loadsAllThreeEncryptedWorkingKeys() {
        val keys = PspLogonWorkingKeys(
            encryptedMacKey = ByteArray(16),
            encryptedPinKey = ByteArray(16),
            encryptedDataKey = ByteArray(16),
        )
        val steps = workflow.logonCompletionSteps(keys)
        assertEquals(3, steps.size)
        assertTrue(steps[0] is PspKeyLoadStep.LoadTmkEncryptedMacKey)
        assertTrue(steps[1] is PspKeyLoadStep.LoadTmkEncryptedPinKey)
        assertTrue(steps[2] is PspKeyLoadStep.LoadTmkEncryptedDataKey)
    }
}
