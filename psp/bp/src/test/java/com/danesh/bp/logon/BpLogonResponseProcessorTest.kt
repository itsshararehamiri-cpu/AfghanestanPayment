package com.danesh.bp.logon

import android.R
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionContextProvider
import com.danesh.bp.bootstrap.BpBootstrapResponseValidator
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.bp.field48.BpMerchantInfoPersister
import com.danesh.bp.support.SupportMenuResponsePersister
import com.danesh.core.Device
import com.danesh.core.MacKeyType
import com.danesh.iso.BpIsoMessage
import com.danesh.iso.IsoMessage
import com.danesh.iso.field48.BpField48Tlv
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BpLogonResponseProcessorTest {

    private val processor = BpLogonResponseProcessor(
        responseValidator = BpBootstrapResponseValidator(BpMacCalculator(FakeDevice())),
        contextProvider = FakeContextProvider(),
        supportMenuPersister = SupportMenuResponsePersister { },
        merchantInfoPersister = BpMerchantInfoPersister(FakeContextProvider()),
    )

    @Test
    fun extractWorkingKeys_splits48ByteField62IntoThree16ByteBlocks() {
        val field62 = ByteArray(48) { index -> (index + 1).toByte() }
        val response = BpIsoMessage(BpField48Tlv()).apply {
            setPrivateUseField62Bytes(field62)
        }

        val keys = processor.extractWorkingKeys(response)

        assertEquals(16, keys.encryptedMacKey.size)
        assertEquals(16, keys.encryptedPinKey.size)
        assertEquals(16, keys.encryptedDataKey.size)
        assertArrayEquals(field62.sliceArray(0..15), keys.encryptedMacKey)
        assertArrayEquals(field62.sliceArray(16..31), keys.encryptedPinKey)
        assertArrayEquals(field62.sliceArray(32..47), keys.encryptedDataKey)
    }

    @Test
    fun extractWorkingKeys_splits72ByteField62IntoThree24ByteBlocks() {
        val field62 = ByteArray(72) { index -> (index + 10).toByte() }
        val response = BpIsoMessage(BpField48Tlv()).apply {
            setPrivateUseField62Bytes(field62)
        }

        val keys = processor.extractWorkingKeys(response)

        assertEquals(24, keys.encryptedMacKey.size)
        assertEquals(24, keys.encryptedPinKey.size)
        assertEquals(24, keys.encryptedDataKey.size)
        assertArrayEquals(field62.sliceArray(0..23), keys.encryptedMacKey)
        assertArrayEquals(field62.sliceArray(24..47), keys.encryptedPinKey)
        assertArrayEquals(field62.sliceArray(48..71), keys.encryptedDataKey)
    }

    @Test(expected = IllegalStateException::class)
    fun extractWorkingKeys_rejectsInvalidField62Length() {
        val response = BpIsoMessage(BpField48Tlv()).apply {
            setPrivateUseField62Bytes(ByteArray(32))
        }
        processor.extractWorkingKeys(response)
    }

    private class FakeDevice : Device {
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
        override suspend fun getMac(data: ByteArray, index: Int, keyType: MacKeyType) = ByteArray(8)
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
        override fun getCheckValue(TT: String): ByteArray {
            return ByteArray(0)
        }
    }

    private class FakeContextProvider : TransactionContextProvider {
        override fun getTerminalConfig() = TerminalConfig(
            terminalId = "T1",
            merchantId = "M1",
            merchantName = "",
            merchantPhone = "",
            nii = "0009",
            pointOfServiceEntryMode = "021",
            currency = "364",
        )

        override fun saveTerminalConfig(config: TerminalConfig) = Unit
        override fun nextStan() = "000001"
        override fun currentClock() = TransactionClock("20260711", "120000")
        override fun saveVatPercentage(varPercentage: String) = Unit
        override fun getVatPercentage(): String = ""
        override fun lastSuccessfulStan(): String = "000000"
        override fun lastSuccessfulRrn(): String = "000000000000"
        override fun saveLastSuccessfulTransaction(stan: String, rrn: String?) = Unit
        override fun clearTerminalData() = Unit
    }
}
