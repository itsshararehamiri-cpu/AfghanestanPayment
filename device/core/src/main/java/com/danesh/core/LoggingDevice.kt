package com.danesh.core

import android.content.Context
import android.graphics.Bitmap

/**
 * Decorator around [Device] that logs every hardware operation via [DeviceTrace].
 */
class LoggingDevice(
    private val delegate: Device,
    private val printErrorNotifier: PrintErrorNotifier,
) : Device {

    override val INDEX_MAC: Int get() = delegate.INDEX_MAC
    override val INDEX_TMK: Int get() = delegate.INDEX_TMK
    override val INDEX_BOOTSTRAP_TMK: Int get() = delegate.INDEX_BOOTSTRAP_TMK
    override val INDEX_BOOTSTRAP_MAC: Int get() = delegate.INDEX_BOOTSTRAP_MAC
    override val INDEX_TEK: Int get() = delegate.INDEX_TEK
    override val INDEX_DATA: Int get() = delegate.INDEX_DATA
    override val INDEX_PIN: Int get() = delegate.INDEX_PIN
    override val hasKeyboard: Boolean get() = delegate.hasKeyboard

    override suspend fun getModel(): String {
        DeviceTrace.step("getModel", "started")
        return delegate.getModel().also {
            DeviceTrace.step("getModel", "result=$it")
        }
    }

    override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
        // فقط index؛ محتوای کلید پایانه لاگ نمی‌شود.
        DeviceTrace.step("writeMasterKey", "inject index=$index")
        delegate.writeMasterKey(masterKey, index)
        DeviceTrace.step("writeMasterKey", "inject index=$index نتیجه=موفق")
    }

    override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?) {
        DeviceTrace.step(
            "writeMacKey",
            "قبل از inject index=$index bytes=${macKey.size} wrappingTmk=${wrappingTmk?.size ?: 0}",
        )
        delegate.writeMacKey(macKey, index, wrappingTmk)
        DeviceTrace.step("writeMacKey", "inject index=$index نتیجه=موفق")
    }

    override suspend fun writeDataKey(dataKey: ByteArray) {
        DeviceTrace.step("writeDataKey", "bytes=${dataKey.size}")
        delegate.writeDataKey(dataKey)
        DeviceTrace.step("writeDataKey", "completed")
    }

    override suspend fun writePinKey(pinKey: ByteArray) {
        DeviceTrace.step("writePinKey", "bytes=${pinKey.size}")
        delegate.writePinKey(pinKey)
        DeviceTrace.step("writePinKey", "completed")
    }

    override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) {
        DeviceTrace.step(
            "loadTmkEncryptedMacKey",
            "قبل از inject index=$index encryptedBytes=${encryptedKey.size}",
        )
        delegate.loadTmkEncryptedMacKey(encryptedKey, index)
        DeviceTrace.step("loadTmkEncryptedMacKey", "inject index=$index نتیجه=موفق")
    }

    override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) {
        DeviceTrace.step(
            "loadTmkEncryptedPinKey",
            "قبل از inject index=$INDEX_PIN encryptedBytes=${encryptedKey.size}",
        )
        delegate.loadTmkEncryptedPinKey(encryptedKey)
        DeviceTrace.step("loadTmkEncryptedPinKey", "inject index=$INDEX_PIN نتیجه=موفق")
    }

    override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) {
        DeviceTrace.step(
            "loadTmkEncryptedDataKey",
            "قبل از inject index=$INDEX_DATA encryptedBytes=${encryptedKey.size}",
        )
        delegate.loadTmkEncryptedDataKey(encryptedKey)
        DeviceTrace.step("loadTmkEncryptedDataKey", "inject index=$INDEX_DATA نتیجه=موفق")
    }

    override fun clearMasterKeyCache() = delegate.clearMasterKeyCache()

    override fun hasWorkingMacKeyOnPed(): Boolean = delegate.hasWorkingMacKeyOnPed()

    @Deprecated("Keys are not cached in app memory")
    override fun peekWorkingMacKey(): ByteArray? = null

    @Deprecated("Keys are not cached in app memory")
    override fun clearWorkingMacKeyCache() = Unit

    @Deprecated("Keys are not cached in app memory")
    override fun restoreMasterKeyCache(masterKey: ByteArray, index: Int) = Unit

    override suspend fun awaitPinpadReady(timeoutMs: Long): Boolean =
        delegate.awaitPinpadReady(timeoutMs)

    override suspend fun getMac(data: ByteArray, index: Int, keyType: MacKeyType): ByteArray {
        DeviceTrace.step(
            "getMac",
            "calc PED makIndex=$index keyType=$keyType inputBytes=${data.size}",
        )
        return delegate.getMac(data, index, keyType).also {
            DeviceTrace.step("getMac", "makIndex=$index resultBytes=${it.size}")
        }
    }

    override suspend fun diagnoseMacMismatch(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        referenceMac: ByteArray,
    ) {
        DeviceTrace.step(
            "diagnoseMacMismatch",
            "makIndex=$index keyType=$keyType inputBytes=${data.size} refLen=${referenceMac.size}",
        )
        delegate.diagnoseMacMismatch(data, index, keyType, referenceMac)
    }

    override suspend fun readCard(
        context: Context,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
        onTimeOut: () -> Unit,
    ) {
        DeviceTrace.step("readCard", "started")
        delegate.readCard(
            context = context,
            onSuccess = { track2, pan ->
                DeviceTrace.step("readCard", "success track2Len=${track2.length} panLen=${pan.length}")
                onSuccess(track2, pan)
            },
            onError = { message ->
                DeviceTrace.warn("readCard", "error: $message")
                onError(message)
            },
            onTimeOut = {
                DeviceTrace.warn("readCard", "timeout")
                onTimeOut()
            },
        )
    }

    override suspend fun getPinBlock(
        context: Context,
        pan: String,
        onError: (String) -> Unit,
        onInput: (Int) -> Unit,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit,
        onTimeOut: () -> Unit,
    ) {
        DeviceTrace.step("getPinBlock", "started panLen=${pan.length}")
        delegate.getPinBlock(
            context = context,
            pan = pan,
            onError = { message ->
                DeviceTrace.warn("getPinBlock", "error: $message")
                onError(message)
            },
            onInput = { length ->
                DeviceTrace.debug("getPinBlock", "input length=$length")
                onInput(length)
            },
            onConfirm = { pinBlock ->
                DeviceTrace.step("getPinBlock", "confirmed pinBlockLen=${pinBlock.length}")
                onConfirm(pinBlock)
            },
            onCancel = {
                DeviceTrace.warn("getPinBlock", "cancelled")
                onCancel()
            },
            onTimeOut = {
                DeviceTrace.warn("getPinBlock", "timeout")
                onTimeOut()
            },
        )
    }

    override  fun getSerial(): String {
        DeviceTrace.step("getSerial", "started")
        return delegate.getSerial().also {
            DeviceTrace.step("getSerial", "result=$it")
        }
    }

    override suspend fun getImei(): String {
        DeviceTrace.step("getImei", "started")
        return delegate.getImei().also {
            DeviceTrace.step("getImei", "resultLen=${it.length}")
        }
    }

    override suspend fun decryptData(
        data: ByteArray,
        onSuccess: (data: ByteArray) -> Unit,
        onError: (String) -> Unit,
    ) {
        DeviceTrace.step("decryptData", "inputBytes=${data.size}")
        delegate.decryptData(
            data = data,
            onSuccess = { decrypted ->
                DeviceTrace.step("decryptData", "success outputBytes=${decrypted.size}")
                onSuccess(decrypted)
            },
            onError = { message ->
                DeviceTrace.warn("decryptData", "error: $message")
                onError(message)
            },
        )
    }

    override suspend fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
        reportErrorToUi: Boolean,
    ) {
        DeviceTrace.step("print", "started ${bitmap.width}x${bitmap.height}")
        delegate.print(
            bitmap = bitmap,
            context = context,
            onSuccess = {
                DeviceTrace.step("print", "success")
                onSuccess()
            },
            onFailed = { message ->
                DeviceTrace.warn("print", "failed: $message")
                if (reportErrorToUi) {
                    printErrorNotifier.notify(message)
                }
                onFailed(message)
            },
            reportErrorToUi = reportErrorToUi,
        )
    }

    override suspend fun getPrinterError(): String {
        DeviceTrace.step("getPrinterError", "started")
        return delegate.getPrinterError().also { error ->
            if (error.isBlank()) {
                DeviceTrace.step("getPrinterError", "ok")
            } else {
                DeviceTrace.warn("getPrinterError", error)
            }
        }
    }

    override fun encrypt(data: ByteArray): ByteArray? {
        DeviceTrace.step("encrypt", "inputBytes=${data.size}")
        return delegate.encrypt(data)
    }

    override fun decrypt(data: ByteArray): ByteArray? {
        DeviceTrace.step("decrypt", "inputBytes=${data.size}")
        return delegate.decrypt(data)
    }

    override fun powerOnIcCard(): Boolean {
        DeviceTrace.step("powerOnIcCard", "started")
        return delegate.powerOnIcCard().also {
            DeviceTrace.step("powerOnIcCard", "result=$it")
        }
    }

    override fun powerOffIcCard() {
        DeviceTrace.step("powerOffIcCard", "started")
        delegate.powerOffIcCard()
        DeviceTrace.step("powerOffIcCard", "completed")
    }

    override fun isIcCardDetect(): Boolean {
        DeviceTrace.step("isIcCardDetect", "started")
        return delegate.isIcCardDetect().also {
            DeviceTrace.step("isIcCardDetect", "result=$it")
        }
    }

    override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit): ByteArray? {
        DeviceTrace.step("sendApdu", "commandBytes=${byteArray.size}")
        return delegate.sendApdu(
            byteArray = byteArray,
            onError = { message ->
                DeviceTrace.warn("sendApdu", "error: $message")
                onError(message)
            },
        )?.also {
            DeviceTrace.step("sendApdu", "responseBytes=${it.size}")
        }
    }

    override suspend fun setDateTime(dataTime: String) {
        DeviceTrace.step("setDateTime", "value=$dataTime")
        delegate.setDateTime(dataTime)
        DeviceTrace.step("setDateTime", "completed")
    }

    override suspend fun getBatteryStatus(): Boolean {
        DeviceTrace.step("getBatteryStatus", "started")
        return delegate.getBatteryStatus().also {
            DeviceTrace.step("getBatteryStatus", "result=$it")
        }
    }

    override fun disableHome() {
        DeviceTrace.step("disableHome", "started")
        delegate.disableHome()
    }

    override fun enableHome() {
        DeviceTrace.step("enableHome", "started")
        delegate.enableHome()
    }

    override fun registerBootAutoStart() {
        DeviceTrace.step("registerBootAutoStart", "started")
        delegate.registerBootAutoStart()
    }

    override suspend fun scan(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit,
    ) {
        DeviceTrace.step("scan", "started")
        delegate.scan(
            context = context,
            onSuccess = { result ->
                DeviceTrace.step("scan", "success resultLen=${result.length}")
                onSuccess(result)
            },
            onError = { message ->
                DeviceTrace.warn("scan", "error: $message")
                onError(message)
            },
            onTimeout = {
                DeviceTrace.warn("scan", "timeout")
                onTimeout()
            },
            onCancel = {
                DeviceTrace.warn("scan", "cancelled")
                onCancel()
            },
        )
    }

    override suspend fun getKCv(): KCV {
        DeviceTrace.step("getKCv", "started")
        return delegate.getKCv().also {
            DeviceTrace.step("getKCv", "completed")
        }
    }

    override fun getCheckValue(TT: String): ByteArray {
        return ByteArray(0)
    }

    override suspend fun beep(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
    ) {
        DeviceTrace.step("beep", "started")
        delegate.beep(
            context = context,
            onSuccess = {
                DeviceTrace.step("beep", "success")
                onSuccess()
            },
            onFailed = { message ->
                DeviceTrace.warn("beep", "failed: $message")
                onFailed(message)
            },
        )
    }

    override suspend fun ledOn(onError: (String) -> Unit) {
        DeviceTrace.step("ledOn", "started")
        delegate.ledOn { message ->
            DeviceTrace.warn("ledOn", "error: $message")
            onError(message)
        }
        DeviceTrace.step("ledOn", "completed")
    }

    override suspend fun ledOff(onError: (String) -> Unit) {
        DeviceTrace.step("ledOff", "started")
        delegate.ledOff { message ->
            DeviceTrace.warn("ledOff", "error: $message")
            onError(message)
        }
        DeviceTrace.step("ledOff", "completed")
    }

    override suspend fun lockNavigationBottom(context: Context) {
        DeviceTrace.step("lockNavigationBottom", "started")
        delegate.lockNavigationBottom(context)
        DeviceTrace.step("lockNavigationBottom", "completed")
    }

}
