package com.danesh.sadad.keycard

import com.danesh.api.DeviceConfigurationStore
import com.danesh.api.DeviceConfigurationSummary
import com.danesh.core.Device
import com.danesh.core.KCV
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher

private class InMemoryKeyPairStore : SadadKeyCardKeyPairStore {
    private val pairs = mutableMapOf<Int, SadadStoredRsaKeyPair>()
    override fun save(keyIndex: Int, modulus: ByteArray, privateExponent: ByteArray) {
        pairs[keyIndex] = SadadStoredRsaKeyPair(modulus, privateExponent)
    }
    override fun load(keyIndex: Int): SadadStoredRsaKeyPair? = pairs[keyIndex]
    override fun clear(keyIndex: Int) {
        pairs.remove(keyIndex)
    }
}

private class FakeDevice : Device {
    override val INDEX_MAC = 1
    override val INDEX_TMK = 1
    override val INDEX_TEK = 1
    override val INDEX_DATA = 1
    override val INDEX_PIN = 1
    override val hasKeyboard = false
    val writtenMasterKeys = mutableMapOf<Int, ByteArray>()
    val writtenMacKeys = mutableMapOf<Int, ByteArray>()
    var writtenDataKey: ByteArray? = null
    var writtenPinKey: ByteArray? = null

    override suspend fun getModel() = "TEST"
    override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
        writtenMasterKeys[index] = masterKey.copyOf()
    }
    override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?) {
        writtenMacKeys[index] = macKey.copyOf()
    }
    override suspend fun writeDataKey(dataKey: ByteArray) {
        writtenDataKey = dataKey.copyOf()
    }
    override suspend fun writePinKey(pinKey: ByteArray) {
        writtenPinKey = pinKey.copyOf()
    }
    override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) = Unit
    override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) = Unit
    override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) = Unit
    override suspend fun getMac(data: ByteArray, index: Int, keyType: com.danesh.core.MacKeyType) = ByteArray(8)
    override suspend fun readCard(context: android.content.Context, onSuccess: (String, String) -> Unit, onError: (String) -> Unit, onTimeOut: () -> Unit) = Unit
    override suspend fun getPinBlock(context: android.content.Context, pan: String, onError: (String) -> Unit, onInput: (Int) -> Unit, onConfirm: (String) -> Unit, onCancel: () -> Unit, onTimeOut: () -> Unit) = Unit
    override fun getSerial() = "SERIAL"
    override suspend fun decryptData(data: ByteArray, onSuccess: (ByteArray) -> Unit, onError: (String) -> Unit) = Unit
    override suspend fun print(bitmap: android.graphics.Bitmap, context: android.content.Context, onSuccess: () -> Unit, onFailed: (String) -> Unit, reportErrorToUi: Boolean) = Unit
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
    override suspend fun scan(context: android.content.Context, onSuccess: (String) -> Unit, onError: (String) -> Unit, onTimeout: () -> Unit, onCancel: () -> Unit) = Unit
    override suspend fun getKCv() = KCV("", "", "", "")
    override fun getCheckValue(tt: String) = ByteArray(0)
    override suspend fun beep(context: android.content.Context, onSuccess: () -> Unit, onFailed: (String) -> Unit) = Unit
    override suspend fun ledOn(onError: (String) -> Unit) = Unit
    override suspend fun ledOff(onError: (String) -> Unit) = Unit
    override suspend fun lockNavigationBottom(context: android.content.Context) = Unit
}

private class FakeConfigurationStore : DeviceConfigurationStore {
    var configured = false
        private set
    override fun isConfigured() = configured
    override fun markConfigured() { configured = true }
    override fun clearConfigured() { configured = false }
    override fun saveConfigurationSummary(summary: DeviceConfigurationSummary) = Unit
    override fun getConfigurationSummary(): DeviceConfigurationSummary? = null
    override fun clearConfigurationSummary() = Unit
}

class SadadKeyCardServiceTest {

    private fun toFixedLength(signed: ByteArray, length: Int): ByteArray {
        val unsigned = if (signed.size > length && signed[0] == 0.toByte()) {
            signed.copyOfRange(1, signed.size)
        } else signed
        val out = ByteArray(length)
        System.arraycopy(unsigned, 0, out, length - unsigned.size, unsigned.size)
        return out
    }

    private fun rsaEncrypt(publicKey: RSAPublicKey, plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(plain)
    }

    @Test
    fun loadKeyPairFromCardA_storesModulusAndExponent() = runBlocking {
        val generator = KeyPairGenerator.getInstance("RSA").apply { initialize(1024) }
        val keyPair = generator.generateKeyPair()
        val privateKey = keyPair.private as RSAPrivateKey
        val modulus = toFixedLength(privateKey.modulus.toByteArray(), 128)
        val exponent = toFixedLength(privateKey.privateExponent.toByteArray(), 128)

        val transport = FakeSadadIccTransport(publicModulus = modulus, privateExponent = exponent)
        val store = InMemoryKeyPairStore()
        val service = SadadKeyCardService(
            reader = SadadKeyCardReader(transport),
            storage = store,
            injector = SadadKeyCardInjector(FakeDevice(), FakeConfigurationStore()),
        )

        val result = service.loadKeyPairFromCardA(pin = "1234", keyIndex = 1)

        assertTrue(result.isSuccess)
        val stored = store.load(1)
        assertArrayEquals(modulus, stored?.modulus)
        assertArrayEquals(exponent, stored?.privateExponent)
    }

    @Test
    fun loadKeyPairFromCardA_wrongPin_returnsPinRejectedFailure() = runBlocking {
        val transport = FakeSadadIccTransport(correctPin = "1234")
        val service = SadadKeyCardService(
            reader = SadadKeyCardReader(transport),
            storage = InMemoryKeyPairStore(),
            injector = SadadKeyCardInjector(FakeDevice(), FakeConfigurationStore()),
        )

        val result = service.loadKeyPairFromCardA(pin = "0000", keyIndex = 1)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SadadPinRejectedException)
        assertEquals(2, (error as SadadPinRejectedException).remainingTries)
    }

    @Test
    fun loadAndInjectMasterKeys_withoutStoredCardAPair_fails() = runBlocking {
        val service = SadadKeyCardService(
            reader = SadadKeyCardReader(FakeSadadIccTransport()),
            storage = InMemoryKeyPairStore(),
            injector = SadadKeyCardInjector(FakeDevice(), FakeConfigurationStore()),
        )

        val result = service.loadAndInjectMasterKeys(SadadKeyCard.CARD_C, pin = "1234", keyIndex = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun fullFlow_cardAThenCardC_decryptsAndInjectsAllWorkingKeys() = runBlocking {
        val generator = KeyPairGenerator.getInstance("RSA").apply { initialize(1024) }
        val keyPair = generator.generateKeyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val modulus = toFixedLength(privateKey.modulus.toByteArray(), 128)
        val exponent = toFixedLength(privateKey.privateExponent.toByteArray(), 128)

        val terminalMasterKey = ByteArray(16) { 0xA1.toByte() }
        val macKey = ByteArray(16) { 0xB2.toByte() }
        val dataKey = ByteArray(16) { 0xC3.toByte() }
        val pinKey = ByteArray(16) { 0xD4.toByte() }
        val initMacKey = ByteArray(16) { 0xE5.toByte() }
        val initDataKey = ByteArray(16) { 0xF6.toByte() }

        val encryptedByNumber = mapOf(
            SadadKeyNumber.TERMINAL_MASTER_KEY.number to rsaEncrypt(publicKey, terminalMasterKey),
            SadadKeyNumber.MAC.number to rsaEncrypt(publicKey, macKey),
            SadadKeyNumber.DATA.number to rsaEncrypt(publicKey, dataKey),
            SadadKeyNumber.INIT_PIN.number to rsaEncrypt(publicKey, pinKey),
            SadadKeyNumber.INIT_MAC.number to rsaEncrypt(publicKey, initMacKey),
            SadadKeyNumber.INIT_DATA.number to rsaEncrypt(publicKey, initDataKey),
        )

        val store = InMemoryKeyPairStore()
        val device = FakeDevice()
        val configStore = FakeConfigurationStore()

        // مرحله ۱: کارت A
        val cardATransport = FakeSadadIccTransport(publicModulus = modulus, privateExponent = exponent)
        val serviceForA = SadadKeyCardService(
            reader = SadadKeyCardReader(cardATransport),
            storage = store,
            injector = SadadKeyCardInjector(device, configStore),
        )
        assertTrue(serviceForA.loadKeyPairFromCardA(pin = "1234", keyIndex = 1).isSuccess)

        // مرحله ۲: کارت C (مستقل، ممکن است در جلسه‌ای دیگر اجرا شود)
        val cardCTransport = FakeSadadIccTransport(encryptedKeysByNumber = encryptedByNumber)
        val serviceForC = SadadKeyCardService(
            reader = SadadKeyCardReader(cardCTransport),
            storage = store,
            injector = SadadKeyCardInjector(device, configStore),
        )
        val result = serviceForC.loadAndInjectMasterKeys(SadadKeyCard.CARD_C, pin = "1234", keyIndex = 1)

        assertTrue(result.exceptionOrNull()?.stackTraceToString() ?: "no error", result.isSuccess)
        assertArrayEquals(terminalMasterKey, device.writtenMasterKeys[device.INDEX_TMK])
        assertArrayEquals(macKey, device.writtenMacKeys[device.INDEX_MAC])
        assertArrayEquals(dataKey, device.writtenDataKey)
        assertArrayEquals(pinKey, device.writtenPinKey)
        assertTrue(configStore.configured)
        assertFalse(cardCTransport.poweredOn)
    }
}
