package com.danesh.knine

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import com.centerm.system.sdk.aidl.DeviceService
import com.centerm.system.sdk.aidl.SystemDevicesFactory
import com.danesh.core.Device
import com.danesh.core.DeviceDefaults
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

class K9 @Inject constructor(
    @ApplicationContext private val context: Context

) : Device {
    private var deviceManager: DeviceManager? = null
    private val cardType: Byte = 0
    override val INDEX_DATA: Int
        get() = 1
    override val INDEX_MAC: Int
        get() = 1
    override val INDEX_TMK: Int
        get() = 1
    override val INDEX_BOOTSTRAP_TMK: Int
        get() = 2
    override val INDEX_BOOTSTRAP_MAC: Int
        get() = 2
    override val INDEX_PIN: Int
        get() = 1
    override val INDEX_TEK: Int
        get() = 1
    override val hasKeyboard: Boolean
        get() = false

    override suspend fun getModel(): String = DEFAULT_MODEL
    private var dataCbcMode: Boolean = false

    private val keyManager = K9KeyManager(
        pinpadProvider = { pinpadOrNull() },
        dataCbcModeProvider = { dataCbcMode },
        indexTmk = INDEX_TMK,
        indexMac = INDEX_MAC,
        indexPin = INDEX_PIN,
        indexData = INDEX_DATA,
    )

    private fun pinpadOrNull() = deviceManager?.pinpadDevice
    private fun preparePinpad(device: PinpadDevice) = keyManager.preparePinpad(device)

    init {
        DevicesFactory.create(context, object : ResultCallback<DeviceManager> {
            override fun onFinish(d: DeviceManager?) {
                if (d == null) {
                } else deviceManager = d
            }

            override fun onError(p0: Int, p1: String?) {
            }

        })

    }

    override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
        // کلید پایانه نباید در لاگ نوشته شود؛ فقط تزریق به PED.
        keyManager.writeMasterKey(masterKey, index)
    }

    override suspend fun writeMacKey(
        macKey: ByteArray, index: Int, wrappingTmk: ByteArray?
    ) {
    }

    suspend fun writeMacKey(macKey: ByteArray, index: Int) {
        keyManager.writePlaintextMacKey(macKey, index)
        Log.d(
            "TAG", "K9K9K9>-writeMacKey${HexUtils.bytesToHexString(macKey)},index=$index"
        )
        Log.d("TAG", "writeMacKey: dddd${getCheckValue()}")
    }

    override suspend fun writeDataKey(dataKey: ByteArray) {
        keyManager.writePlaintextDataKey(dataKey)
        Log.d(
            "TAG", "K9K9K9>-writeDataKey${HexUtils.bytesToHexString(dataKey)}"
        )
        Log.d("TAG", "writeDataKey: ${getCheckValue()}")

    }

    override suspend fun writePinKey(pinKey: ByteArray) {
        keyManager.writePlaintextPinKey(pinKey)
        Log.d(
            "TAG", "K9K9K9>-writePinKey${HexUtils.bytesToHexString(pinKey)}"
        )
        Log.d("TAG", "writePinKey: ${getCheckValue()}")

    }

    override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) {
        keyManager.loadTmkEncryptedMacKey(encryptedKey, index)
        Log.d(
            "TAG",
            "K9K9K9>-loadTmkEncryptedMacKey${HexUtils.bytesToHexString(encryptedKey)},index=$index"
        )
        Log.d("TAG", "loadTmkEncryptedMacKey: ${getCheckValue()}")

    }

    override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) {
        keyManager.loadTmkEncryptedPinKey(encryptedKey)
        Log.d(
            "TAG", "K9K9K9>-loadTmkEncryptedPinKey${HexUtils.bytesToHexString(encryptedKey)}"
        )
        Log.d("TAG", "loadTmkEncryptedPinKey: ${getCheckValue()}")

    }

    override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) {
        keyManager.loadTmkEncryptedDataKey(encryptedKey)
        Log.d(
            "TAG", "K9K9K9>-loadTmkEncryptedDataKey${HexUtils.bytesToHexString(encryptedKey)}"
        )
        Log.d("TAG", "loadTmkEncryptedDataKey: ${getCheckValue()}")

    }

    override fun clearMasterKeyCache() {
        keyManager.clearMasterKeyCache()
        Log.d(
            "TAG", "K9K9K9>-clearMasterKeyCache"
        )
    }

    override fun peekWorkingMacKey(): ByteArray? {
        Log.d(
            "TAG", "K9K9K9>-peekWorkingMacKey"
        )
        return keyManager.peekWorkingMacKey()
    }

    override fun clearWorkingMacKeyCache() {

        keyManager.clearWorkingMacKeyCache()
        Log.d(
            "TAG", "K9K9K9>-clearWorkingMacKeyCache"
        )
    }

    override fun restoreMasterKeyCache(masterKey: ByteArray, index: Int) {
        keyManager.restoreMasterKeyCache(masterKey, index)
        Log.d(
            "TAG", "K9K9K9>-restoreMasterKeyCache"
        )
    }

    override suspend fun awaitPinpadReady(timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (pinpadOrNull() != null) {
                DeviceTrace.step(SDK, "awaitPinpadReady ok")
                return true
            }
            delay(50)
        }
        val ready = pinpadOrNull() != null
        if (!ready) {
            DeviceTrace.warn(SDK, "awaitPinpadReady timeout ${timeoutMs}ms")
        }
        return ready
    }

    override suspend fun getMac(data: ByteArray, index: Int, keyType: MacKeyType): ByteArray {
        Log.d(
            "TAG",
            "K9K9K9>-getMac,data=${HexUtils.bytesToHexString(data)},index=$index,ket=$keyType"
        )
      //  return keyManager.getMac(data, index, keyType)
        return ByteArray(8)
    }

    override suspend fun diagnoseMacMismatch(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType,
        referenceMac: ByteArray,
    ) {
        keyManager.diagnoseMacMismatch(data, index, keyType, referenceMac)
        Log.d(
            "TAG", "K9K9K9>-diagnoseMacMismatch"
        )
    }

    private fun isValidDesBlockSize(data: ByteArray): Boolean =
        data.isNotEmpty() && data.size % 8 == 0

    override suspend fun readCard(
        context: Context,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
        onTimeOut: () -> Unit
    ) {
        if (deviceManager == null) {
            onError(
                errorMessage(
                    context,
                    R.string.error_card_reader_connection,
                    context.getString(R.string.device_is_not_ready)
                )
            )
        } else {
            val magCardDevice: MagCardDevice = deviceManager!!.magneticDevice
            magCardDevice.swipeCard(
                20000, true, object : IMagCardListener.Stub() {
                    override fun onSwipeCardTimeout() {
                        onTimeOut()
                    }

                    override fun onSwipeCardException(i: Int) {
                        onError(errorMessage(context, R.string.error_card_swipe, sdkError(i)))
                    }

                    override fun onSwipeCardSuccess(trackData: TrackData?) {
                        if (trackData != null) {
                            val track2 = decodeTrack2("${trackData.secondTrackData}")
//
                           /*      onSuccess(track2, trackData.cardno)*/
                            onSuccess("9004230100000016=31042210000000000000","9004230100000016")
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

    override suspend fun getPinBlock(
        context: Context,
        pan1: String,
        onError: (String) -> Unit,
        onInput: (Int) -> Unit,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit,
        onTimeOut: () -> Unit
    ) {
        val pan="9004230100000016"
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
        val pinPadInfo = PinPadInfo.builder(pan).setPikId(INDEX_PIN).setShowInputBox(true)
            .setUseRandomKeybord(false).setMinLength(4).setMaxLength(4)// TODO:  
            .setPinLengthFilter(byteArrayOf(4)).setBeep(true).setCancelable(true)
            .setEncrpyMode(PinPadInfo.EncrpyMode.MODE_ZERO).setShowMask(true)
            .setExMessage(context.getString(R.string.password_hint)).build()
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
                onConfirm(HexUtils.bcd2str(pinBlock))
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
        DeviceTrace.warn(SDK, "getSerial SN unavailable â€” fallback DeviceDefaults.SERIAL")
        return DeviceDefaults.SERIAL
    }

    override suspend fun getImei(): String {
        val fromHw = runCatching {
            deviceManager?.systemDevice?.getSystemInfo(SystemInfoType.IMEI)?.trim().orEmpty()
        }.getOrDefault("")
        // Dual-SIM SDK Ú¯Ø§Ù‡ÛŒ `imei1-imei2` Ù…ÛŒâ€ŒØ¯Ù‡Ø¯Ø› Ø¨Ø±Ø§ÛŒ F63 ÙÙ‚Ø· IMEI Ø§ÙˆÙ„.
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
        Log.d(
            "TAG", "K9K9K9>-decryptData"
        )
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
        val decryptedData = pinpad.decryptDataByDek(INDEX_DATA, data, dataCbcMode)
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
            onFailed(errorMessage(context, R.string.error_print_device, "deviceManager=null"))
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
                    onFailed(
                        resolveKnownDeviceError(
                            context, code, R.string.error_printer, message
                        )
                    )
                }
            })
        } catch (e: Exception) {
            DeviceTrace.error(SDK, "print failed", throwable = e)
            onFailed(errorMessage(context, R.string.error_printer, exceptionDetail(e)))
        }
    }

    fun addTestData(bitmap: Bitmap, mPrinter: PrinterDevice, context: Context) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            mPrinter.addBitmapPrintItem(stream.toByteArray())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getPrinterError(): String {
        if (deviceManager == null) {
            return errorMessage(context, R.string.error_print_device, "deviceManager=null")
        }
        val device = deviceManager!!.printDevice
        return try {
            device.clearBufferArea()
            val bundle = Bundle()
            val printerState = device.printSync(bundle)
            when {
                printerState.stateCode == PrinterState.PRINTER_STATE_NOPAPER.toInt() -> errorMessage(
                    context,
                    R.string.error_printer_no_paper,
                    "stateCode=${printerState.stateCode}",
                )

                printerState.stateCode.toShort() == PrinterState.PRINTER_STATE_NORMAL -> ""
                else -> errorMessage(
                    context,
                    R.string.error_printer,
                    "stateCode=${printerState.stateCode}",
                )
            }
        } catch (e: Exception) {
            DeviceTrace.error(SDK, "getPrinterError failed", throwable = e)
            errorMessage(context, R.string.error_printer, exceptionDetail(e))
        }
    }

    override fun encrypt(data: ByteArray): ByteArray? {
        Log.d(
            "TAG", "K9K9K9>-encrypt"
        )
        val pinpad = pinpadOrNull() ?: run {
            DeviceTrace.error(SDK, "encrypt pinpadDevice unavailable")
            return null
        }
        if (!isValidDesBlockSize(data)) {
            DeviceTrace.warn(SDK, "encrypt invalid data length=${data.size}")
            return null
        }
        preparePinpad(pinpad)
        val encrypted = pinpad.encryptDataByDek(INDEX_DATA, data, dataCbcMode)
        if (encrypted == null || encrypted.isEmpty()) {
            DeviceTrace.warn(SDK, "encryptDataByDek returned empty")
            return null
        }
        DeviceTrace.step(SDK, "encryptDataByDek success outputBytes=${encrypted.size}")
        return encrypted
    }

    override fun decrypt(data: ByteArray): ByteArray? {
        Log.d(
            "TAG", "K9K9K9>decrypt"
        )
        val pinpad = pinpadOrNull() ?: run {
            DeviceTrace.error(SDK, "decrypt pinpadDevice unavailable")
            return null
        }
        if (!isValidDesBlockSize(data)) {
            DeviceTrace.warn(SDK, "decrypt invalid data length=${data.size}")
            return null
        }
        preparePinpad(pinpad)
        val decrypted = pinpad.decryptDataByDek(INDEX_DATA, data, dataCbcMode)
        if (decrypted == null || decrypted.isEmpty()) {
            DeviceTrace.warn(SDK, "decryptDataByDek returned empty")
            return null
        }
        DeviceTrace.step(SDK, "decryptDataByDek success outputBytes=${decrypted.size}")
        return decrypted
    }

    override fun powerOnIcCard(): Boolean {
        return false
    }

    override fun powerOffIcCard() {

    }

    override fun isIcCardDetect(): Boolean {
        return false
    }

    override suspend fun sendApdu(byteArray: ByteArray, onError: (String) -> Unit): ByteArray? {
        var mIcCardDevice: IcCardDevice? = null
        if (deviceManager == null) {
            mIcCardDevice?.halt()
            onError(errorMessage(context, R.string.error_icc_device, "deviceManager=null"))
        } else {
            mIcCardDevice = deviceManager!!.icDevice
            val result = mIcCardDevice.reset()
            DeviceTrace.debug(SDK, "sendApdu reset resultLen=${result?.size ?: 0}")
            if (mIcCardDevice.exists()) {
                val result = mIcCardDevice.send(byteArray)
                mIcCardDevice?.halt()

                return result
                //  showNormalMessage("Send result = " + HexUtils.bcd2str(result))
            } else {
                mIcCardDevice?.halt()

                onError(
                    errorMessage(
                        context, R.string.error_icc_card_not_found, "iccCard.exists=false"
                    )
                )

            }
        }
        mIcCardDevice?.halt()
        return null
    }

    /**
     * ØªÙ†Ø¸ÛŒÙ… Ø³Ø§Ø¹Øª Ø³ÛŒØ³ØªÙ… Ø§Ø² ÙÛŒÙ„Ø¯ 7 Ù…ÛŒØ²Ø¨Ø§Ù† (ÙØ±Ù…Øª YYMMDDHHMISS).
     * Ø§Ø² [SystemDevice.setSystemTime] Ø§Ø³ØªÙØ§Ø¯Ù‡ Ù…ÛŒâ€ŒÚ©Ù†Ø¯.
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
                //scanning timeout. default 60000ms.
                scanParams.putInt(IScanner.TIMEOUT, 60_000)
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

    private fun autoStopScan() {
//        showNormalMessage("start scanning and will be stoped 5s later.")
//        scan()
//        Handler().postDelayed(object : Runnable {
//            override fun run() {
//                scanner.stopScan()
//            }
//        }, 5000)
    }

//    private fun decode(scanner: ScanDevice) {
//        try {
//            val bitmap = BitmapFactory.decodeStream(requireActivity().getAssets().open("image/qr_code.png"))
//            val data =  Bundle()
//            data.putByteArray(IScanner.DATA, DataUtil.getYUVByBitmap(bitmap))
//            data.putInt(IScanner.WIDTH, bitmap.getWidth())
//            data.putInt(IScanner.HEIGHT, bitmap.getHeight())
//            val result = scanner.decode(data)
//          //  showNormalMessage("decode result is " + HexUtils.bcd2str(result))
//        } catch ( e:IOException) {
//            e.printStackTrace()
//        }
//    }

    override suspend fun getKCv(): KCV {
        return KCV("", "", "", "")
    }

    override fun getCheckValue(tt: String): ByteArray {
        Log.d(
            "TAG", "getCheckValuedmac->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_MAC, keyType = KeyType.MAK
                    )
                )
            }"
        )
        Log.d(
            "TAG", "getCheckValuedpdin->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_TMK, keyType = KeyType.TEK
                    )
                )
            }"
        )
        Log.d(
            "TAG", "getCheckValuedpin->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_PIN, keyType = KeyType.PIK
                    )
                )
            }"
        )
        Log.d(
            "TAG", "getCheckValuedmdata->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_DATA, keyType = KeyType.DEK
                    )
                )
            }"
        )
        Log.d(
            "TAG", "getCheckValuedmdata->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_TMK, keyType = KeyType.TEK
                    )
                )
            }"
        )
        Log.d(
            "TAG", "getCheckValuedmdata->${
                HexUtils.bytesToHexString(
                    keyManager.getCheckValue(
                        INDEX_BOOTSTRAP_TMK, keyType = KeyType.TEK
                    )
                )
            }"
        )

        return keyManager.getCheckValue(INDEX_MAC, keyType = KeyType.MAK)
    }

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
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

                override fun onError(i: Int, s: String?) {
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
//    private suspend fun level3LoadingTest() {
//        delay(3000)
//        val device1=deviceManager!!.pinpadDevice
//        val tekId = INDEX_TEK
//        val tmkId = INDEX_MK
//        val pikId =INDEX_PIN
//        val makId = INDEX_WK
//        val tdkId = 1
//        val dekId = 1
//
//        val tek = HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//        val tmk = HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//        val pik =HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//        val mak = HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//        val tdk = HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//        val dek =HexUtils.hexStringToByte("29E9C9CCC2A0021020DBCAB8964B54F2")
//
//        showNormalMessage("------------------------------------\n3-Level Keys Loading Test:\n------------------")
//        try {
//            var errorFlag = false
//            fun performLoad(label: String, success: Boolean, id: Int, key: ByteArray): Boolean {
//                val keyStr = HexUtils.bcd2str(key)
//                if (success) {
//                    showNormalMessage("Loading $label:\nID: $id | Key: $keyStr\nResult: true\n------------------")
//                } else {
//                    showErrorMessage("Loading $label:\nID: $id | Key: $keyStr\nResult: false\n------------------")
//                }
//                return success
//            }
//val cbc=false
//            // Load Keys
//            errorFlag = errorFlag or !performLoad("TEK", device.loadTek(tekId, tek), tekId, tek)
//
//            val enTmk = DES3Utils.encrypt3DES(tmk, tek, cbc)
//            errorFlag = errorFlag or !performLoad("TMK", device.loadTekEncryptedTmk(tekId, tmkId, enTmk), tmkId, tmk)
//
//            val enPik = DES3Utils.encrypt3DES(pik, tmk, cbc)
//            errorFlag = errorFlag or !performLoad("PIK", device.loadTmkEncryptedPik(tmkId, pikId, enPik), pikId, pik)
//
//            val enMak = DES3Utils.encrypt3DES(mak, tmk, cbc)
//            errorFlag = errorFlag or !performLoad("MAK", device.loadTmkEncryptedMak(tmkId, makId, enMak), makId, mak)
//
//            val enTdk = DES3Utils.encrypt3DES(tdk, tmk, cbc)
//            errorFlag = errorFlag or !performLoad("TDK", device.loadTmkEncryptedTdk(tmkId, tdkId, enTdk), tdkId, tdk)
//
//            //if (device.isSupportDEK) {
//                val enDek = DES3Utils.encrypt3DES(dek, tmk, cbc)
//                errorFlag = errorFlag or !performLoad("DEK", device.loadTmkEncryptedDek(tmkId, dekId, enDek), dekId, dek)
//           // }
//
//            // CV Checks
//            fun checkCv(label: String, key: ByteArray, keyType: KeyType, id: Int): Boolean {
//                val calcCv = DES3Utils.getCheckValue(HexUtils.bcd2str(key))
//                val readCv = HexUtils.bcd2str(device.getCheckValue(keyType, id))
//                return if (calcCv == readCv) {
//                    showNormalMessage("$label Check Value: $calcCv âˆšâˆšâˆš")
//                    true
//                } else {
//                    showErrorMessage("$label Check Value Error\nCalc cv: $calcCv\nRead cv: $readCv\n------------------")
//                    false
//                }
//            }
//
//            errorFlag = errorFlag or !checkCv("TEK", tek, KeyType.TEK, tekId)
//            errorFlag = errorFlag or !checkCv("TMK", tmk, KeyType.TDKEK, tmkId) // Note: Using TDKEK as in your original comment
//            errorFlag = errorFlag or !checkCv("PIK", pik, KeyType.PIK, pikId)
//            errorFlag = errorFlag or !checkCv("MAK", mak, KeyType.MAK, makId)
//            errorFlag = errorFlag or !checkCv("TDK", tdk, KeyType.TDK, tdkId)
//
//         //   if (device.su) {
//                errorFlag = errorFlag or !checkCv("DEK", dek, KeyType.DEK, dekId)
//         //   }
//
//            if (errorFlag) {
//                showErrorMessage("-------------------------------------\n---------Test Failï¼---------\n-------------------------------------")
//            } else {
//                showNormalMessage("-------------------------------------\n---------Test Passï¼---------\n-------------------------------------")
//            }
//
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            showErrorMessage(e.message ?: "Unknown Error")
//        }
//    }

    private fun showNormalMessage(string: String) {
        DeviceTrace.debug(SDK, "showNormalMessage $string")
    }

    private fun showErrorMessage(string: String) {
        DeviceTrace.warn(SDK, "showErrorMessage $string")
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
