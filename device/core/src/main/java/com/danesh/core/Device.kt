package com.danesh.core

import android.content.Context
import android.graphics.Bitmap

interface Device {
    val INDEX_MAC: Int
    val INDEX_TMK: Int
    val INDEX_BOOTSTRAP_TMK: Int get() = INDEX_TMK
    val INDEX_BOOTSTRAP_MAC: Int get() = INDEX_MAC
    val INDEX_TEK: Int

    val INDEX_DATA: Int
    val INDEX_PIN: Int
    val hasKeyboard:Boolean
    suspend fun getModel(): String

    suspend fun writeMasterKey(masterKey: ByteArray, index: Int = INDEX_TMK)
    suspend fun writeMacKey(macKey: ByteArray, index: Int = INDEX_MAC, wrappingTmk: ByteArray? = null)
    suspend fun writeDataKey(dataKey: ByteArray)
    suspend fun writePinKey(pinKey: ByteArray)
    suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int = INDEX_MAC)
    suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray)
    suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray)
    fun clearMasterKeyCache() {}

    fun hasWorkingMacKeyOnPed(): Boolean = false

    @Deprecated("Keys are not cached in app memory")
    fun peekWorkingMacKey(): ByteArray? = null

    @Deprecated("Keys are not cached in app memory")
    fun clearWorkingMacKeyCache() {}

    @Deprecated("Keys are not cached in app memory")
    fun restoreMasterKeyCache(masterKey: ByteArray, index: Int = INDEX_TMK) {}
    suspend fun awaitPinpadReady(timeoutMs: Long = 30_000L): Boolean = true
    suspend  fun getMac(
        data: ByteArray,
        index: Int = INDEX_MAC,
        keyType: MacKeyType = MacKeyType.WORK,
    ): ByteArray


    suspend fun diagnoseMacMismatch(
        data: ByteArray,
        index: Int = INDEX_MAC,
        keyType: MacKeyType = MacKeyType.WORK,
        referenceMac: ByteArray,
    ) {
    }
    suspend fun readCard(context: Context, onSuccess: (String, String) -> Unit, onError: (String) -> Unit, onTimeOut: () -> Unit)
    suspend fun getPinBlock(context: Context,
                            pan: String, onError: (String) -> Unit,
                            onInput: (Int) -> Unit, onConfirm: (String) -> Unit,
                            onCancel: () -> Unit, onTimeOut: () -> Unit
    )

    fun getSerial(): String
    suspend fun getImei(): String = ""

    suspend fun decryptData(data: ByteArray,onSuccess: (data: ByteArray) -> Unit,onError: (String)->Unit)
    suspend fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
        reportErrorToUi: Boolean = true,
    )
    suspend fun getPrinterError(

    ): String
    fun encrypt(data: ByteArray): ByteArray?
    fun decrypt(data: ByteArray): ByteArray?
    fun powerOnIcCard(): Boolean
    fun powerOffIcCard()
    fun isIcCardDetect(): Boolean

    suspend fun sendApdu(byteArray: ByteArray,onError: (String) -> Unit): ByteArray?
    suspend fun setDateTime(dataTime: String)
    suspend fun getBatteryStatus(): Boolean
    fun disableHome()
    fun enableHome()
    fun registerBootAutoStart() {}
    suspend fun scan(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    )
    suspend  fun getKCv(): KCV
    fun  getCheckValue(tt: String=""): ByteArray
    suspend fun beep(context: Context,onSuccess: () -> Unit,onFailed: (String) -> Unit)
    suspend fun ledOn(onError: (String) -> Unit)
    suspend fun ledOff(onError: (String) -> Unit)
    suspend fun lockNavigationBottom(context: Context)

}
