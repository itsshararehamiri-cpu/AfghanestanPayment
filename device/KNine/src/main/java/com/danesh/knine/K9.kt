package com.danesh.knine

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.StringRes
import com.centerm.system.sdk.aidl.DeviceService
import com.centerm.system.sdk.aidl.SystemDevicesFactory
import com.danesh.core.Device
import com.danesh.core.DeviceDefaults
import com.danesh.core.DeviceKeyIndexes
import com.danesh.core.DeviceKeyType
import com.danesh.core.DeviceSettings
import com.danesh.core.DeviceSettingsProvider
import com.danesh.core.KeyLoadError
import com.danesh.core.KeyLoadResult
import com.danesh.core.DeviceTrace
import com.danesh.core.KCV
import com.danesh.core.MacKeyType
import com.pos.sdk.DeviceManager
import com.pos.sdk.DevicesFactory
import com.pos.sdk.callback.ResultCallback
import com.pos.sdk.iccard.IcCardDevice
import com.pos.sdk.led.LedColor
import com.pos.sdk.led.LedDevice
import com.pos.sdk.magcard.IMagCardListener
import com.pos.sdk.magcard.MagCardDevice
import com.pos.sdk.magcard.TrackData
import com.pos.sdk.pinpad.IPinPadPinCallback
import com.pos.sdk.pinpad.KeyType
import com.pos.sdk.pinpad.PinPadInfo
import com.pos.sdk.pinpad.PinpadDevice
import com.pos.sdk.printer.IPrinterResultListener
import com.pos.sdk.printer.PrinterDevice
import com.pos.sdk.printer.PrinterState
import com.pos.sdk.scan.IScanCallback
import com.pos.sdk.scan.IScanner
import com.pos.sdk.scan.ScanDevice
import com.pos.sdk.sys.SystemDevice
import com.pos.sdk.sys.SystemDevice.SystemInfoType
import com.pos.util.HexUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


private const val SDK = "K9.SDK"
private const val DEFAULT_MODEL = "K9"

/**
 * پیاده‌سازی [Device] روی دستگاه K9 (SDK سنترم).
 *
 * این کلاس فقط به SDK دستگاه وابسته است: اندیس کلیدها، زمان‌های انتظار، طول رمز و الگوریتم MAC
 * همه از [DeviceSettingsProvider] (پیکربندی برنامه / PSP فعال) خوانده می‌شوند.
 */
class K9 @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsProvider: DeviceSettingsProvider,
) : Device {
    private var deviceManager: DeviceManager? = null
    private var mIcCardDevice: IcCardDevice? = null

    /**
     * هر `deviceManager.icDevice` در سرویس سنترم یک `open` جدید روی کارت‌خوان انجام می‌دهد؛
     * برای polling حضور کارت یک نمونه نگه داشته می‌شود و در [powerOffIcCard] بسته می‌شود.
     */
    private var icDeviceHandle: IcCardDevice? = null

    private fun icDeviceOrNull(): IcCardDevice? {
        icDeviceHandle?.let { return it }
        val manager = deviceManager ?: return null
        return runCatching { manager.icDevice }.getOrNull()?.also { icDeviceHandle = it }
    }

    private val settings: DeviceSettings get() = settingsProvider.settings()
    private val keyIndexes: DeviceKeyIndexes get() = settings.keyIndexes

    override val INDEX_DATA: Int get() = keyIndexes.dataKey
    override val INDEX_MAC: Int get() = keyIndexes.macKey
    override val INDEX_TMK: Int get() = keyIndexes.masterKey
    override val INDEX_BOOTSTRAP_TMK: Int get() = keyIndexes.bootstrapMasterKey
    override val INDEX_BOOTSTRAP_MAC: Int get() = keyIndexes.bootstrapMacKey
    override val INDEX_PIN: Int get() = keyIndexes.pinKey
    override val INDEX_TEK: Int get() = keyIndexes.transportKey
    override val hasKeyboard: Boolean
        get() = false

    override suspend fun getModel(): String = DEFAULT_MODEL
    private var dataCbcMode: Boolean = false

    private val keyManager = K9KeyManager(
        pinpadProvider = { pinpadOrNull() },
        dataCbcModeProvider = { dataCbcMode },
        indexes = { keyIndexes },
        defaultMacTypeName = { settings.macAlgorithm },
    )

    private fun pinpadOrNull() = deviceManager?.pinpadDevice
    private fun preparePinpad(device: PinpadDevice) = keyManager.preparePinpad(device)

    init {
        DevicesFactory.create(context, object : ResultCallback<DeviceManager> {
            override fun onFinish(d: DeviceManager?) {
                if (d == null) {
                    DeviceTrace.error(SDK, "DevicesFactory.create returned null DeviceManager")
                } else {
                    deviceManager = d
                }
            }

            override fun onError(code: Int, message: String?) {
                DeviceTrace.error(SDK, "DevicesFactory.create failed ${sdkError(code, message)}")
            }

        })

    }

    override suspend fun writeMasterKey(masterKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.MASTER, index) { keyManager.writeMasterKey(masterKey, index) }

    /** [wrappingTmk] لازم نیست: TMK فعال (آخرین [writeMasterKey]) برای رمز کردن کلید استفاده می‌شود. */
    override suspend fun writeMacKey(macKey: ByteArray, index: Int, wrappingTmk: ByteArray?): KeyLoadResult =
        keyOperation(DeviceKeyType.MAC, index) { keyManager.writePlaintextMacKey(macKey, index) }

    override suspend fun writeDataKey(dataKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.DATA, index) { keyManager.writePlaintextDataKey(dataKey, index) }

    override suspend fun writePinKey(pinKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.PIN, index) { keyManager.writePlaintextPinKey(pinKey, index) }

    override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.MAC, index) { keyManager.loadTmkEncryptedMacKey(encryptedKey, index) }

    override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.PIN, index) { keyManager.loadTmkEncryptedPinKey(encryptedKey, index) }

    override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray, index: Int): KeyLoadResult =
        keyOperation(DeviceKeyType.DATA, index) { keyManager.loadTmkEncryptedDataKey(encryptedKey, index) }

    /** اجرای عملیات کلید و تبدیل هر خطا به [KeyLoadResult.Failure] (هیچ exception بیرون نمی‌رود). */
    private suspend fun keyOperation(
        keyType: DeviceKeyType,
        index: Int,
        block: suspend () -> Unit,
    ): KeyLoadResult = try {
        block()
        KeyLoadResult.Success(keyType, index)
    } catch (error: kotlinx.coroutines.CancellationException) {
        throw error
    } catch (error: Exception) {
        val failure = KeyLoadResult.Failure(
            keyType = keyType,
            index = index,
            error = classifyKeyError(error),
            message = exceptionDetail(error),
            cause = error,
        )
        DeviceTrace.error(SDK, "$keyType key load failed index=$index error=${failure.error}", throwable = error)
        failure
    }

    private fun classifyKeyError(error: Throwable): KeyLoadError {
        val message = error.message.orEmpty()
        return when {
            pinpadOrNull() == null || message.contains("pinpad unavailable", ignoreCase = true) ->
                KeyLoadError.DEVICE_UNAVAILABLE
            message.contains("KCV mismatch", ignoreCase = true) -> KeyLoadError.KCV_MISMATCH
            message.contains("TMK", ignoreCase = false) && message.contains("not loaded|missing".toRegex()) ->
                KeyLoadError.MASTER_KEY_MISSING
            message.contains("length", ignoreCase = true) -> KeyLoadError.INVALID_KEY
            message.contains("PED", ignoreCase = false) && message.contains("failed") -> KeyLoadError.REJECTED_BY_DEVICE
            else -> KeyLoadError.UNKNOWN
        }
    }

    override fun clearMasterKeyCache() {
        keyManager.clearMasterKeyCache()
    }

    override fun peekWorkingMacKey(): ByteArray? {
        return keyManager.peekWorkingMacKey()
    }

    override fun clearWorkingMacKeyCache() {

        keyManager.clearWorkingMacKeyCache()
    }

    override fun restoreMasterKeyCache(masterKey: ByteArray, index: Int) {
        keyManager.restoreMasterKeyCache(masterKey, index)
    }

    override suspend fun awaitPinpadReady(timeoutMs: Long?): Boolean {
        val effectiveTimeout = timeoutMs ?: settings.timeouts.pinpadReadyMs
        val deadline = System.currentTimeMillis() + effectiveTimeout
        while (System.currentTimeMillis() < deadline) {
            if (pinpadOrNull() != null) {
                DeviceTrace.step(SDK, "awaitPinpadReady ok")
                return true
            }
            delay(50)
        }
        val ready = pinpadOrNull() != null
        if (!ready) {
            DeviceTrace.warn(SDK, "awaitPinpadReady timeout ${effectiveTimeout}ms")
        }
        return ready
    }

    override suspend fun getMac(data: ByteArray, index: Int, keyType: MacKeyType): ByteArray =
        keyManager.getMac(data, index, keyType)

    override suspend fun getMacWithType(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        macType: String,
    ): ByteArray = keyManager.getMacWithType(data, index, keyType, macType)

    override suspend fun diagnoseMacMismatch(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        referenceMac: ByteArray,
    ) {
        keyManager.diagnoseMacMismatch(data, index, keyType, referenceMac)
    }

    private fun isValidDesBlockSize(data: ByteArray): Boolean =
        data.isNotEmpty() && data.size % 8 == 0

    override suspend fun readCard(
        context: Context,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
        onTimeOut: () -> Unit
    ) {
        val magCardDevice: MagCardDevice? = runCatching { deviceManager?.magneticDevice }.getOrNull()
        if (magCardDevice == null) {
            onError(
                errorMessage(
                    context,
                    R.string.error_card_reader_connection,
                    context.getString(R.string.device_is_not_ready)
                )
            )
        } else {
            magCardDevice.swipeCard(
                settings.timeouts.cardReadMs, true, object : IMagCardListener.Stub() {
                    override fun onSwipeCardTimeout() {
                        onTimeOut()
                    }

                    override fun onSwipeCardException(i: Int) {
                        onError(errorMessage(context, R.string.error_card_swipe, sdkError(i)))
                    }

                    override fun onSwipeCardSuccess(trackData: TrackData?) {
                        if (trackData != null) {
                            val track2 = decodeTrack2("${trackData.secondTrackData}")
                            onSuccess("38$track2", trackData.cardno)
                        } else onError(
                            errorMessage(
                                context,
                                R.string.error_card_read,
                                context.getString(R.string.swipe_card_fail)
                            )
                        )
                    }


                    override fun onSwipeCardFail() {
                        onError(
                            errorMessage(
                                context,
                                R.string.error_card_read,
                                context.getString(R.string.error_card_swipe)
                            )
                        )
                    }

                    override fun onCancelSwipeCard() {
                        onError(context.getString(R.string.error_card_read_cancelled))

                    }
                })
        }

    }

    override suspend fun getPinBlock(title:String,
        context: Context,
        pan: String,
        onError: (String) -> Unit,
        onInput: (Int) -> Unit,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit,
        onTimeOut: () -> Unit
    ) {
        val pinPad = pinpadOrNull()
        if (pinPad == null) {
            onError(
                errorMessage(
                    context,
                    R.string.error_device_access,
                    context.getString(R.string.device_is_not_ready)
                )
            )
            return
        }
        if (pan.length < 16) {
            onError(
                errorMessage(
                    context,
                    R.string.error_card_swipe,
                    context.getString(R.string.invalid_card_information_plz_try_again)
                )
            )
            return
        }
        preparePinpad(pinPad)
        val policy = settings.pinEntry
        val pinLengths = (policy.minLength..policy.maxLength).map { it.toByte() }.toByteArray()
        val pinPadInfo = PinPadInfo.builder(pan).setPikId(keyIndexes.pinEntryKey).setShowInputBox(true)
            .setUseRandomKeybord(false).setMinLength(policy.minLength).setMaxLength(policy.maxLength)
            .setPinLengthFilter(pinLengths).setBeep(true).setCancelable(true)
            .setEncrpyMode(PinPadInfo.EncrpyMode.MODE_ZERO).setShowMask(true)
            .setExMessage(if(title.isEmpty())context.getString(R.string.password_hint) else title).build()
        pinPad.getPin(pinPadInfo, object : IPinPadPinCallback.Stub() {
            override fun onReadingPin(length: Int, masked: String?) {
                onInput(length)
            }

            override fun onReadPinCancel() {
                onCancel()
            }

            override fun onReadPinSuccess(pinBlock: ByteArray?) {
                if (pinBlock == null || pinBlock.isEmpty()) {
                    onError(
                        errorMessage(
                            context,
                            R.string.error_pin_read,
                            context.getString(R.string.pin_could_not_be_processed_please_enter_your_pin_again)
                        )
                    )
                    return
                }
                onConfirm(HexUtils.bytesToHexString(pinBlock))
            }

            override fun onError(code: Int, message: String?) {
                onError(resolveKnownDeviceError(context, code, R.string.error_pin_read, message))
            }
        })
    }

    override fun getSerial(): String {
        val fromHw = runCatching {
            deviceManager?.systemDevice?.getSystemInfo(SystemInfoType.SN)?.trim().orEmpty()
        }.getOrDefault("")
        if (fromHw.isNotEmpty()) {
            DeviceTrace.step(SDK, "getSerial from SystemInfoType.SN len=${fromHw.length}")
            return fromHw
        }
        DeviceTrace.warn(SDK, "getSerial SN unavailable - fallback DeviceDefaults.SERIAL")
        return DeviceDefaults.SERIAL
    }

    override suspend fun getImei(): String {
        val fromHw = runCatching {
            deviceManager?.systemDevice?.getSystemInfo(SystemInfoType.IMEI)?.trim().orEmpty()
        }.getOrDefault("")
        // SDK دوسیم‌کارته گاهی `imei1-imei2` می‌دهد؛ فقط IMEI اول برگردانده می‌شود.
        val single =
            fromHw.split('-', ',', '/', ';').map { it.trim() }.firstOrNull { it.isNotEmpty() }
                .orEmpty().filter { it.isDigit() }.ifEmpty { fromHw.trim() }
        if (single.isNotEmpty()) {
            DeviceTrace.step(SDK, "getImei from SystemInfoType.IMEI len=${single.length}")
        } else {
            DeviceTrace.warn(SDK, "getImei IMEI unavailable")
        }
        return single
    }

    override suspend fun decryptData(
        data: ByteArray, onSuccess: (data: ByteArray) -> Unit, onError: (String) -> Unit
    ) {
        val pinpad = pinpadOrNull()
        if (pinpad == null) {
            onError(errorMessage(context, R.string.error_device_unavailable, "deviceManager=null"))
            return
        }
        if (!isValidDesBlockSize(data)) {
            onError(
                errorMessage(
                    context,
                    R.string.error_decrypt_data,
                    "data.length=${data.size}",
                )
            )
            return
        }

        preparePinpad(pinpad)
        val decryptedData = runCatching { pinpad.decryptDataByDek(INDEX_DATA, data, dataCbcMode) }
            .onFailure { DeviceTrace.error(SDK, "decryptDataByDek failed", throwable = it) }
            .getOrNull()
        if (decryptedData == null || decryptedData.isEmpty()) {
            onError(
                errorMessage(
                    context,
                    R.string.error_decrypt_data,
                    "decryptDataByDek=null",
                )
            )
        } else {
            DeviceTrace.step(SDK, "decryptDataByDek success outputBytes=${decryptedData.size}")
            onSuccess(decryptedData)
        }
    }

    override suspend fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit, reportErrorToUi: Boolean,
    ) {
        if (deviceManager == null) {
            DeviceTrace.warn(SDK, "print failed deviceManager=null")
            onFailed(K9PrinterErrorMessages.deviceUnavailable(context))
            return
        }
        val mPrinter = deviceManager!!.printDevice
        val bundle = Bundle()
        mPrinter.printSync(bundle)
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
           mPrinter.addBitmapPrintItem(stream.toByteArray())
            mPrinter.print(bundle, object : IPrinterResultListener.Stub() {
                override fun onPrintFinish() {
                    DeviceTrace.step(SDK, "print onPrintFinish")
                    onSuccess()
                }

                override fun onPrintError(code: Int, message: String?) {
                    DeviceTrace.warn(SDK, "print onPrintError code=$code message=$message")
                    onFailed(K9PrinterErrorMessages.message(context, code))
                }
            })
        } catch (e: Exception) {
            DeviceTrace.error(SDK, "print failed", throwable = e)
            onFailed(K9PrinterErrorMessages.generic(context))
        }
    }

    override suspend fun getPrinterError(): String {
        if (deviceManager == null) {
            DeviceTrace.warn(SDK, "getPrinterError deviceManager=null")
            return K9PrinterErrorMessages.deviceUnavailable(context)
        }
        val device = deviceManager!!.printDevice
        return try {
            device.clearBufferArea()
            val bundle = Bundle()
            val printerState = device.printSync(bundle)
            when {
                printerState.stateCode == PrinterState.PRINTER_STATE_NOPAPER.toInt() ->
                    K9PrinterErrorMessages.noPaper(context)

                printerState.stateCode.toShort() == PrinterState.PRINTER_STATE_NORMAL -> ""
                else -> {
                    DeviceTrace.warn(SDK, "printer stateCode=${printerState.stateCode}")
                    K9PrinterErrorMessages.generic(context)
                }
            }
        } catch (e: Exception) {
            DeviceTrace.error(SDK, "getPrinterError failed", throwable = e)
            K9PrinterErrorMessages.generic(context)
        }
    }

    override fun encrypt(data: ByteArray): ByteArray? {
        val pinpad = pinpadOrNull() ?: run {
            DeviceTrace.error(SDK, "encrypt pinpadDevice unavailable")
            return null
        }
        if (!isValidDesBlockSize(data)) {
            DeviceTrace.warn(SDK, "encrypt invalid data length=${data.size}")
            return null
        }
        preparePinpad(pinpad)
        val encrypted = runCatching { pinpad.encryptDataByDek(INDEX_DATA, data, dataCbcMode) }
            .onFailure { DeviceTrace.error(SDK, "encryptDataByDek failed", throwable = it) }
            .getOrNull()
        if (encrypted == null || encrypted.isEmpty()) {
            DeviceTrace.warn(SDK, "encryptDataByDek returned empty")
            return null
        }
        DeviceTrace.step(SDK, "encryptDataByDek success outputBytes=${encrypted.size}")
        return encrypted
    }

    override fun decrypt(data: ByteArray): ByteArray? {
        val pinpad = pinpadOrNull() ?: run {
            DeviceTrace.error(SDK, "decrypt pinpadDevice unavailable")
            return null
        }
        if (!isValidDesBlockSize(data)) {
            DeviceTrace.warn(SDK, "decrypt invalid data length=${data.size}")
            return null
        }
        preparePinpad(pinpad)
        val decrypted = runCatching { pinpad.decryptDataByDek(INDEX_DATA, data, dataCbcMode) }
            .onFailure { DeviceTrace.error(SDK, "decryptDataByDek failed", throwable = it) }
            .getOrNull()
        if (decrypted == null || decrypted.isEmpty()) {
            DeviceTrace.warn(SDK, "decryptDataByDek returned empty")
            return null
        }
        DeviceTrace.step(SDK, "decryptDataByDek success outputBytes=${decrypted.size}")
        return decrypted
    }

    override fun powerOnIcCard(): Boolean {
        val icCardDevice = icDeviceOrNull()
        if (icCardDevice == null) {
            DeviceTrace.warn(SDK, "powerOnIcCard deviceManager=null")
            return false
        }
        val atr = runCatching { icCardDevice.reset() }.getOrNull()
        if (atr == null) {
            DeviceTrace.warn(SDK, "powerOnIcCard reset failed")
            mIcCardDevice = null
            return false
        }
        DeviceTrace.step(SDK, "powerOnIcCard success atrLen=${atr.size}")
        mIcCardDevice = icCardDevice
        return true
    }

    override fun powerOffIcCard() {
        val icCardDevice = mIcCardDevice ?: icDeviceHandle
        mIcCardDevice = null
        icDeviceHandle = null
        if (icCardDevice == null) return
        try {
            icCardDevice.halt()
            DeviceTrace.step(SDK, "powerOffIcCard halted")
        } catch (e: Exception) {
            DeviceTrace.warn(SDK, "powerOffIcCard halt failed: ${exceptionDetail(e)}")
        }
    }

    override fun isIcCardDetect(): Boolean {
        val icCardDevice = mIcCardDevice ?: icDeviceOrNull() ?: return false

        return runCatching { icCardDevice.exists() }.getOrDefault(false)
    }

    override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit): ByteArray? {
        val icCardDevice = mIcCardDevice
        if (deviceManager == null || icCardDevice == null) {
            onError(errorMessage(context, R.string.error_icc_device, "deviceManager=null"))
            return null
        }
        if (!icCardDevice.exists()) {
            onError(
                errorMessage(
                    context, R.string.error_icc_card_not_found, "iccCard.exists=false"
                )
            )
            return null
        }
        return runCatching { icCardDevice.send(byteArray) }
            .onFailure { error ->
                DeviceTrace.error(SDK, "sendApdu failed", throwable = error)
                onError(errorMessage(context, R.string.error_icc_device, exceptionDetail(error)))
            }
            .getOrNull()
    }

    /**
     * تنظیم ساعت سیستم از فیلد 7 میزبان (فرمت YYMMDDHHMISS) با [SystemDevice.setSystemTime].
     */
    override suspend fun setDateTime(dataTime: String) {
        val normalized = dataTime.trim()
        if (normalized.length != 12 || !normalized.all { it.isDigit() }) {
            DeviceTrace.warn(SDK, "setDateTime invalid YYMMDDHHMISS '$dataTime'")
            return
        }
        val millis = runCatching {
            SimpleDateFormat("yyMMddHHmmss", Locale.US).apply {
                isLenient = false
            }.parse(normalized)?.time
        }.getOrNull()
        if (millis == null) {
            DeviceTrace.warn(SDK, "setDateTime parse failed for '$normalized'")
            return
        }
        val systemDevice = deviceManager?.systemDevice
        val ok = systemDevice?.setSystemTime(millis) ?: false
        DeviceTrace.step(SDK, "setDateTime $normalized -> $millis setSystemTime=$ok")
    }

    override suspend fun getBatteryStatus(): Boolean {
        return false
    }

    override fun disableHome() {

    }

    override fun enableHome() {

    }

    override suspend fun scan(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        var scanner: ScanDevice? = null
        if (deviceManager == null) {

            onError(errorMessage(context, R.string.error_device_unavailable, "deviceManager=null"))
        } else {
            scanner = deviceManager?.scanDevice
            if (scanner == null) {
                onError(errorMessage(context, R.string.error_scan_device, "scanDevice=null"))
            } else {
                val scanParams = Bundle()
                //camera id, 1 is back, 0 is front. default is back.
                scanParams.putInt(IScanner.CAMERA_ID, 1)
                //torch switch. default false
                scanParams.putBoolean(IScanner.TORCH, false)
                // زمان انتظار اسکن از تنظیمات دستگاه.
                scanParams.putInt(IScanner.TIMEOUT, settings.timeouts.scanMs)
                //beep after scanning finish. default false
                scanParams.putBoolean(IScanner.BEEP, true)
                //scanning continuously. default false.
                scanParams.putBoolean(IScanner.CONTINUOUS, false)
                scanner.scan(scanParams, object : IScanCallback.Stub() {
                    override fun onSuccess(bytes: ByteArray?) {
                        if (bytes == null || bytes.isEmpty()) {
                            onError(
                                errorMessage(
                                    context, R.string.error_scan_failed, "empty scan result"
                                )
                            )
                            return
                        }
                        val text = String(bytes, Charsets.UTF_8).trim('\u0000').trim()
                        onSuccess(text)
                    }

                    override fun onFailed(p0: Int, p1: String?) {
                        DeviceTrace.warn(SDK, "scan onFailed code=$p0 message=$p1")
                        onError(errorMessage(context, R.string.error_scan_failed, sdkError(p0, p1)))
                    }
                })
            }

        }
    }

    override suspend fun getKCv(): KCV = getKcvAt(INDEX_MAC)

    override suspend fun getKcvAt(index: Int): KCV {
        fun hex(keyType: KeyType): String {
            val bytes = runCatching { keyManager.getCheckValue(index, keyType) }.getOrNull()
                ?: return ""
            if (bytes.isEmpty()) return ""
            return runCatching { HexUtils.bytesToHexString(bytes) }
                .getOrDefault("")
                .filter { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }
                .take(6)
                .uppercase()
        }
        return KCV(
            data = hex(KeyType.DEK),
            mac = hex(KeyType.MAK),
            pin = hex(KeyType.PIK),
            master = hex(KeyType.TDKEK),
        )
    }

    override fun getCheckValue(tt: String): ByteArray =
        keyManager.getCheckValue(INDEX_MAC, keyType = KeyType.MAK)

    override suspend fun beep(
        context: Context, onSuccess: () -> Unit, onFailed: (String) -> Unit
    ) {
        val manager = deviceManager
        if (manager == null) {
            onFailed(errorMessage(context, R.string.error_device_unavailable, "deviceManager=null"))
            return
        }
        try {
            manager.beepDevice?.beep(0)
            delay(2000)
            manager.beepDevice?.beep(1)
            delay(2000)
            manager.beepDevice?.beep(2)
            onSuccess()
        } catch (e: Exception) {
            DeviceTrace.warn(SDK, "beep failed cause=${e.cause} message=${e.message}")
            onFailed(errorMessage(context, R.string.error_beep, exceptionDetail(e)))
        }
    }

    override suspend fun ledOff(onError: (String) -> Unit) {
        var ledDevice: LedDevice? = null
        if (deviceManager == null) {

            onError(errorMessage(context, R.string.error_device_unavailable, "deviceManager=null"))
        } else {
            ledDevice = deviceManager?.ledDevice
            if (ledDevice == null) {
                onError(errorMessage(context, R.string.error_led_device, "ledDevice=null"))
            } else {
                ledDevice.turnOff(LedColor.RED)
                delay(1000)
                ledDevice.turnOff(LedColor.YELLOW)
                delay(1000)
                ledDevice.turnOff(LedColor.BLUE)
                delay(1000)
                ledDevice.turnOff(LedColor.GREEN)

            }
        }
    }

    override suspend fun lockNavigationBottom(context: Context) {
        SystemDevicesFactory.create(
            context,
            object : com.centerm.system.sdk.aidl.callback.ResultCallback<DeviceService?> {
                override fun onFinish(deviceService: DeviceService?) {
                    val systemOperation=deviceService?.systemOperation
                    try {
                        systemOperation?.setDisplayNavigationBar(1)
                    } catch (e: Exception) {
                        DeviceTrace.warn(SDK, "setDisplayNavigationBar failed: ${exceptionDetail(e)}")
                    }
                }

                override fun onError(code: Int, message: String?) {
                    DeviceTrace.warn(SDK, "SystemDevicesFactory.create failed ${sdkError(code, message)}")
                }
            })
    }

    override suspend fun ledOn(onError: (String) -> Unit) {
        var ledDevice: LedDevice? = null
        if (deviceManager == null) {

            onError(errorMessage(context, R.string.error_device_unavailable, "deviceManager=null"))
        } else {
            ledDevice = deviceManager?.ledDevice
            if (ledDevice == null) {
                onError(errorMessage(context, R.string.error_led_device, "ledDevice=null"))
            } else {
                ledDevice.turnOn(LedColor.RED)
                delay(1000)
                ledDevice.turnOn(LedColor.YELLOW)
                delay(1000)
                ledDevice.turnOn(LedColor.BLUE)
                delay(1000)
                ledDevice.turnOn(LedColor.GREEN)

            }
        }
    }
    private fun resolveKnownDeviceError(
        context: Context,
        code: Int,
        @StringRes fallbackRes: Int,
        sdkMessage: String? = null,
    ): String = K9PrinterErrorMessages.knownMessage(context, code) ?: errorMessage(
        context, fallbackRes, sdkError(code, sdkMessage)
    )

    private fun errorMessage(
        context: Context,
        @StringRes messageRes: Int,
        detail: String? = null,
    ): String {
        val message = context.getString(messageRes)
        return if (detail.isNullOrBlank()) message
        else {
            context.getString(R.string.error_with_detail, message, detail)
        }
    }

    private fun sdkError(code: Int, message: String? = null): String = buildString {
        append("code=$code")
        if (!message.isNullOrBlank()) append(", msg=$message")
    }

    private fun exceptionDetail(e: Throwable): String =
        e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName

    /**
     * K9 SDK returns track2 as hex-encoded ASCII (e.g. "3530..." -> "50...").
     * ISO8583 field 35 expects the decoded track2 string (max 37 chars).
     */
    private fun decodeTrack2(raw: String): String {
        if (raw.contains('=') || raw.contains('^')) return raw
        if (raw.length % 2 != 0 || !raw.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) {
            return raw
        }
        return buildString(raw.length / 2) {
            var i = 0
            while (i < raw.length - 1) {
                append(raw.substring(i, i + 2).toInt(16).toChar())
                i += 2
            }
        }
    }

}
