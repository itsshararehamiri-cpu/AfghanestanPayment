package com.danesh.sadad.logon

import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.sadad.key.SadadWorkingMacState
import com.danesh.sadad.keycard.SadadKeyCardCrypto
import com.danesh.sadad.keycard.SadadKeyCardMasterKeys
import com.danesh.sadad.keycard.SadadWrappingKeyHolder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SadadLogonWorkingKeyInjectorTest {

    @Test
    fun injectsField48WorkingKeysAtCardCPlusOne() = runBlocking {
        val wrapTmk = ByteArray(16) { 0x11 }
        val wrapPin = ByteArray(16) { 0x44 }
        val wrapMac = ByteArray(16) { 0x22 }
        val wrapData = ByteArray(16) { 0x33 }
        val workingPin = ByteArray(16) { 0xA1.toByte() }
        val workingMac = ByteArray(16) { 0xB2.toByte() }
        val workingData = ByteArray(16) { 0xC3.toByte() }

        val wrapping = SadadWrappingKeyHolder.inMemoryForTests()
        wrapping.storeFromCard(
            SadadKeyCardMasterKeys(
                terminalMasterKey = wrapTmk.copyOf(),
                macKey = wrapMac.copyOf(),
                dataKey = wrapData.copyOf(),
                pinKey = wrapPin.copyOf(),
                initMacKey = ByteArray(16) { 0x55 },
                initDataKey = ByteArray(16) { 0x66 },
            ),
        )
        val macState = SadadWorkingMacState.inMemoryForTests()
        macState.saveKeyIndices(cardCIndex = 16, rsaKeyIndex = 5)
        val device = RecordingDevice()
        val injector = SadadLogonWorkingKeyInjector(device, wrapping, macState)

        injector.inject(
            SadadLogonField48(
                tmsNeed = false,
                changeKeyNeed = true,
                pinKey = SadadKeyCardCrypto.encrypt3DesEcb(workingPin, wrapTmk).toHex(),
                macKey = SadadKeyCardCrypto.encrypt3DesEcb(workingMac, wrapMac).toHex(),
                dataKey = SadadKeyCardCrypto.encrypt3DesEcb(workingData, wrapData).toHex(),
            ),
        )

        assertArrayEquals(wrapTmk, device.writtenMasterKeys[16])
        assertArrayEquals(workingPin, device.writtenPinKeys[17])
        assertArrayEquals(workingMac, device.writtenMacKeys[17])
        assertArrayEquals(workingData, device.writtenDataKeys[17])
        assertTrue(device.writtenPinKeys.containsKey(16).not())
        assertTrue(device.writtenMacKeys.containsKey(16).not())
        assertTrue(device.writtenDataKeys.containsKey(16).not())
        assertEquals(16, macState.initMacIndex())
        assertEquals(17, macState.workingKeyIndex())
        assertTrue(macState.hasWorkingMac())
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { byte -> "%02X".format(byte.toInt() and 0xFF) }

    private class RecordingDevice : Device {
        override val INDEX_MAC = 1
        override val INDEX_TMK = 1
        override val INDEX_TEK = 1
        override val INDEX_DATA = 1
        override val INDEX_PIN = 1
        override val hasKeyboard = false
        val writtenMasterKeys = mutableMapOf<Int, ByteArray>()
        val writtenMacKeys = mutableMapOf<Int, ByteArray>()
        val writtenDataKeys = mutableMapOf<Int, ByteArray>()
        val writtenPinKeys = mutableMapOf<Int, ByteArray>()

        override suspend fun getModel() = "TEST"
        override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
            writtenMasterKeys[index] = masterKey.copyOf()
        }
        override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?) {
            writtenMacKeys[index] = macKey.copyOf()
        }
        override suspend fun writeDataKey(dataKey: ByteArray) = writeDataKey(dataKey, INDEX_DATA)
        override suspend fun writeDataKey(dataKey: ByteArray, index: Int) {
            writtenDataKeys[index] = dataKey.copyOf()
        }
        override suspend fun writePinKey(pinKey: ByteArray) = writePinKey(pinKey, INDEX_PIN)
        override suspend fun writePinKey(pinKey: ByteArray, index: Int) {
            writtenPinKeys[index] = pinKey.copyOf()
        }
        override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) = Unit
        override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) = Unit
        override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) = Unit
        override suspend fun getMac(data: ByteArray, index: Int, keyType: com.danesh.core.MacKeyType) = ByteArray(8)
        override suspend fun readCard(
            context: android.content.Context,
            onSuccess: (String, String) -> Unit,
            onError: (String) -> Unit,
            onTimeOut: () -> Unit,
        ) = Unit
        override suspend fun getPinBlock(
            title: String,
            context: android.content.Context,
            pan: String,
            onError: (String) -> Unit,
            onInput: (Int) -> Unit,
            onConfirm: (String) -> Unit,
            onCancel: () -> Unit,
            onTimeOut: () -> Unit,
        ) = Unit
        override fun getSerial() = "SERIAL"
        override suspend fun decryptData(
            data: ByteArray,
            onSuccess: (ByteArray) -> Unit,
            onError: (String) -> Unit,
        ) = Unit
        override suspend fun print(
            bitmap: android.graphics.Bitmap,
            context: android.content.Context,
            onSuccess: () -> Unit,
            onFailed: (String) -> Unit,
            reportErrorToUi: Boolean,
        ) = Unit
        override suspend fun getPrinterError() = ""
        override fun encrypt(data: ByteArray) = data
        override fun decrypt(data: ByteArray) = data
        override fun powerOnIcCard() = true
        override fun powerOffIcCard() = Unit
        override fun isIcCardDetect() = true
        override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit): ByteArray? = null
        override suspend fun setDateTime(dataTime: String) = Unit
        override suspend fun getBatteryStatus() = false
        override fun disableHome() = Unit
        override fun enableHome() = Unit
        override suspend fun scan(
            context: android.content.Context,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit,
            onTimeout: () -> Unit,
            onCancel: () -> Unit,
        ) = Unit
        override suspend fun getKCv() = KCV("", "", "", "")
        override fun getCheckValue(tt: String) = ByteArray(0)
        override suspend fun beep(
            context: android.content.Context,
            onSuccess: () -> Unit,
            onFailed: (String) -> Unit,
        ) = Unit
        override suspend fun ledOn(onError: (String) -> Unit) = Unit
        override suspend fun ledOff(onError: (String) -> Unit) = Unit
        override suspend fun lockNavigationBottom(context: android.content.Context) = Unit
    }
}
