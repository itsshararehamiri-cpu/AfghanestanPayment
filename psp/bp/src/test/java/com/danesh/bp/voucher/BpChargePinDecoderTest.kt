package com.danesh.bp.voucher

import com.danesh.bp.key.decodeHexKey
import com.danesh.core.Device
import com.danesh.core.KCV
import com.danesh.core.MacKeyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BpChargePinDecoderTest {

    @Test
    fun formatDecryptedChargePin_keepsLeadingFCharacters() {
        assertEquals(
            "FFFF12345678901234",
            BpChargePinDecoder.formatDecryptedChargePin("FFFF12345678901234".toByteArray()),
        )
        assertEquals(
            "ffff12345678901234",
            BpChargePinDecoder.formatDecryptedChargePin("ffff12345678901234".toByteArray()),
        )
    }

    @Test
    fun formatDecryptedChargePin_keepsPinWithoutPadding() {
        assertEquals(
            "987654321098",
            BpChargePinDecoder.formatDecryptedChargePin("987654321098".toByteArray()),
        )
    }

    @Test
    fun formatDecryptedChargePin_trimsNullPadding() {
        assertEquals(
            "1234",
            BpChargePinDecoder.formatDecryptedChargePin("1234\u0000\u0000".toByteArray()),
        )
    }

    @Test
    fun decode_decryptsWithDekAndKeepsLeadingF() {
        val device = FakeDevice(
            decryptResult = "FFFF555566667777".toByteArray(Charsets.ISO_8859_1),
        )
        val decoder = BpChargePinDecoder(device)

        val pin = decoder.decode("A1B2C3D4E5F60718")

        assertEquals("FFFF555566667777", pin)
        assertTrue(device.lastDecryptInput.contentEquals("A1B2C3D4E5F60718".decodeHexKey()))
    }

    private class FakeDevice(
        private val decryptResult: ByteArray?,
    ) : Device {
        var lastDecryptInput: ByteArray = ByteArray(0)

        override val INDEX_MAC = 1
        override val INDEX_TMK = 1
        override val INDEX_TEK = 1
        override val INDEX_DATA = 1
        override val INDEX_PIN = 1
        override val hasKeyboard = false

        override suspend fun getModel(): String = "K9"
        override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) = Unit
        override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?) = Unit
        override suspend fun writeDataKey(dataKey: ByteArray) = Unit
        override suspend fun writePinKey(pinKey: ByteArray) = Unit
        override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) = Unit
        override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) = Unit
        override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) = Unit
        override suspend fun getMac(
            data: ByteArray,
            index: Int,
            keyType: MacKeyType,
        ): ByteArray = ByteArray(8)

        override suspend fun readCard(
            context: android.content.Context,
            onSuccess: (String, String) -> Unit,
            onError: (String) -> Unit,
            onTimeOut: () -> Unit,
        ) = Unit

        override suspend fun getPinBlock(
            context: android.content.Context,
            pan: String,
            onError: (String) -> Unit,
            onInput: (Int) -> Unit,
            onConfirm: (String) -> Unit,
            onCancel: () -> Unit,
            onTimeOut: () -> Unit,
        ) = Unit

        override suspend fun getSerial(): String = "SERIAL"
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

        override suspend fun getPrinterError(): String = ""
        override fun encrypt(data: ByteArray): ByteArray? = data
        override fun decrypt(data: ByteArray): ByteArray? {
            lastDecryptInput = data.copyOf()
            return decryptResult?.copyOf()
        }

        override fun powerOnIcCard(): Boolean = false
        override fun powerOffIcCard() = Unit
        override fun isIcCardDetect(): Boolean = false
        override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit): ByteArray? = null
        override suspend fun setDateTime(dataTime: String) = Unit
        override suspend fun getBatteryStatus(): Boolean = true
        override fun disableHome() = Unit
        override fun enableHome() = Unit
        override suspend fun scan(
            context: android.content.Context,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit,
            onTimeout: () -> Unit,
            onCancel: () -> Unit,
        ) = Unit

        override suspend fun getKCv(): KCV = KCV("", "", "", "")
        override fun getCheckValue(): ByteArray = ByteArray(0)
        override suspend fun beep(
            context: android.content.Context,
            onSuccess: () -> Unit,
            onFailed: (String) -> Unit,
        ) = Unit

        override suspend fun ledOn(onError: (String) -> Unit) = Unit
        override suspend fun ledOff(onError: (String) -> Unit) = Unit
    }
}
