package com.danesh.knine
import com.danesh.core.MacKeyType
import com.danesh.core.DeviceDefaults
import com.danesh.core.DeviceTrace
import com.danesh.core.KCV
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.danesh.core.Device
import com.pos.util.HexUtils
import com.urovo.sdk.magcard.MagCardReaderImpl
import com.urovo.sdk.magcard.listener.MagCardListener
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.urovo.sdk.pinpad.utils.Constant.KeyType
import com.urovo.sdk.print.PrinterProviderImpl
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream


private const val SDK = "K9.SDK"
private const val DEFAULT_MODEL = "K9"

class KNine @Inject constructor(
    @ApplicationContext private val context: Context

) : Device {
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
    private val pinPad: PinPadProviderImpl = PinPadProviderImpl.getInstance()

    override suspend fun getModel(): String = DEFAULT_MODEL
    override suspend fun writeMasterKey(masterKey: ByteArray, index: Int) {
        pinPad.deleteKey(KeyType.MAIN_KEY, INDEX_TMK)
        pinPad.deleteKey(KeyType.MAC_KEY, INDEX_MAC)
        pinPad.deleteKey(KeyType.ENCDEC_KEY, INDEX_TEK)
        pinPad.deleteKey(KeyType.PIN_KEY, INDEX_PIN)
        val result = pinPad.loadMainKey(
            INDEX_TMK,
            HexUtils.hexStringToByte("0123456789ABCDEFFEDCBA9876543210"),
            null
        )
    }

    override suspend fun writeMacKey(
        macKey: ByteArray,
        index: Int,
        wrappingTmk: ByteArray?
    ) {
        val result = pinPad.loadWorkKey(KeyType.MAC_KEY, INDEX_TMK, index, macKey, null)
        Log.d(TAG, "writeMacKey: dddddd$result")


    }

    override suspend fun writeDataKey(dataKey: ByteArray) {
    }

    override suspend fun writePinKey(pinKey: ByteArray) {
        val result = pinPad.loadWorkKey(KeyType.PIN_KEY, INDEX_TMK, INDEX_PIN, pinKey, null)

    }

    override suspend fun loadTmkEncryptedMacKey(encryptedKey: ByteArray, index: Int) {
        val result = pinPad.loadEncryptMainKey(INDEX_MAC,KeyType.MAC_KEY, encryptedKey, null)

    }

    override suspend fun loadTmkEncryptedPinKey(encryptedKey: ByteArray) {
        val result = pinPad.loadEncryptMainKey(INDEX_PIN,KeyType.PIN_KEY, HexUtils.hexStringToByte("31A7364CAC91CA39C0489F69BEC54FA2"), null)
        Log.d(TAG, "loadTmkEncryptedPinKey: dddddd$result")
        getCheckValue("fff")

    }

    override suspend fun loadTmkEncryptedDataKey(encryptedKey: ByteArray) {
    }

    override suspend fun getMac(
        data: ByteArray,
        index: Int,
        keyType: MacKeyType
    ): ByteArray {
        val result = pinPad.calcMAC(INDEX_MAC,data, 0x11)
        return result
    }

    override suspend fun readCard(
        context: Context,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
        onTimeOut: () -> Unit
    ) {
        val magCardReader = MagCardReaderImpl.getInstance()
        try {
            magCardReader.searchCard(30, object : MagCardListener {
                override fun onSuccess(track: Bundle) {
                    if (!track.getString("TRACK2").isNullOrEmpty()) {
                        onSuccess(track.getString("TRACK2")!!,"9004230100000027")
                    } else {
                        onError(context.getString(R.string.swipe_card_fail))
                    }
                }

                override fun onError(error: Int, message: String) {
                    Log.d(TAG, "onError() called with: error = $error, message = $message")
                    onError("$message $error")
                }

                override fun onTimeout() {
                    Log.d(TAG, "onTimeout() called")
                    onTimeOut()
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "readCard cause: ${e.cause}")
            Log.d(TAG, "readCard: ${e.message}")
            onError("${e.cause}")
        }
    }

    override suspend fun getPinBlock(
        context: Context,
        pan: String,
        onError: (String) -> Unit,
        onInput: (Int) -> Unit,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit,
        onTimeOut: () -> Unit
    ) {
        val pinPadBundle = Bundle()
        pinPadBundle.putString("cardNo", pan)
        pinPadBundle.putBoolean("sound", false)
        pinPadBundle.putBoolean("bypass", true)
        pinPadBundle.putString("supportPinLen", "0,4")
        pinPadBundle.putBoolean("customization", false)
        pinPadBundle.putBoolean("FullScreen", true)
        pinPadBundle.putBoolean("onlinePin", true)
        pinPadBundle.putInt("PINKeyNo", INDEX_PIN)
        pinPadBundle.putLong(
            "timeOutMS",
            (30 * 1000).toLong()
        )
//        pinPadBundle.putString("title", context.getString(R.string.enter_pin))
//
//        pinPadBundle.putString("message", context.getString(R.string.required_by_customer))
//        pinPadBundle.putString("head", context.getString(R.string.voucher_pin))

        pinPadBundle.putString("infoLocation", "CENTER")
        pinPadBundle.putBoolean("randomKeyboard", false)
        pinPadBundle.putInt("fontSize", 30)
        //  val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
//        val persianNumbers = context.resources.getStringArray(R.array.numbers)
//        pinPadBundle.putStringArray("numberText", persianNumbers)
        pinPadBundle.putBoolean("randomKeyboardLocation", false)
        val textSize = shortArrayOf(25, 25, 25, 25, 25, 25, 25)
        pinPadBundle.putShortArray("textSize", textSize)
//        pinPadBundle.putString("cancelText", context.getString(R.string.cancel_))
//        pinPadBundle.putString("deleteText", context.getString(R.string.delete))
//        pinPadBundle.putString("okText", context.getString(R.string.confirm))
//        pinPadBundle.putString("message", context.getString(R.string.plz_enter_pin))
//
//        val textColor = intArrayOf(
//            -0xa8de,  // عنوان اول
//            -0xde690d,  // عنوان دوم
//            Color.BLACK,
//            Color.BLACK,
//            Color.BLACK,
//            Color.BLACK,
//            Color.BLACK
//        )

//        pinPadBundle.putIntArray("textColor", textColor)
        pinPad.getPinBlockEx(pinPadBundle, object : PinInputListener {
            override fun onInput(p0: Int, p1: Int) {
                onInput(p0)
            }

            override fun onConfirm(p0: ByteArray?, p1: Boolean) {
                Log.d(TAG, "onConfirm: $p1")
                Log.d(TAG, "onConfiddrm: dddd${HexUtils.bytesToHexString(p0)}")
                if (p0 != null)
                    onConfirm(String(p0))

            }

            override fun onConfirm_dukpt(p0: ByteArray?, p1: ByteArray?) {
                println("onConfirm_dukpt")
            }

            override fun onCancel() {
                Log.d("TAG", "onCancel: ")
                onCancel()
            }

            override fun onTimeOut() {
                Log.d("TAG", "onTimeOut: ")
                onTimeOut()
            }

            override fun onError(p0: Int) {
                onError("CODE ${p0}")
            }
        })
    }

    override fun getSerial(): String {
       return ""
    }

    override suspend fun decryptData(
        data: ByteArray,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
    }

    override suspend fun print(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit,
        reportErrorToUi: Boolean
    ) {
        val printManager: PrinterProviderImpl? = PrinterProviderImpl.getInstance(context)
        try {
            if (printManager == null) {
                onFailed(context.getString(R.string.amount))
            } else {
                printManager.initPrint()
                var status = printManager.status
                if (status != 0) {
                    when (status) {
                        240 -> onFailed(context.getString(R.string.balance_failed_title))
                        247 -> {
                            onFailed(context.getString(com.urovo.urovosdklibs.R.string.charge))
                        }

                        243 -> onFailed(context.getString(R.string.duplicate_receipt))
                        else -> onFailed(context.getString(com.urovo.urovosdklibs.R.string.charge))
                    }
                    printManager.close()
                    return
                }
                printManager.setGray(6)
                status = printManager.status
                if (status != 0) {
                    when (status) {
                        240 -> onFailed(context.getString(R.string.amount))
                        247 -> onFailed(context.getString(R.string.balance_failed_title))
                        243 -> onFailed(context.getString(com.urovo.urovosdklibs.R.string.charge))
                        else -> onFailed(context.getString(R.string.duplicate_receipt))
                    }
                    printManager.close()
                    return
                }
                val imageData = getBitMapBytes(bitmap)
                val format = Bundle()
                format.putInt("align", 1)
                format.putInt("offset", 0)
                format.putInt("height", bitmap.height + 5)
                printManager.addImage(format, imageData)
                printManager.feedLine(-1)
                printManager.startPrint()
                printManager.status
                delay(1000)
                status = printManager.status
                if (status == 0) {
                    onSuccess()
                } else {
                    when (status) {
                        240 -> onFailed(context.getString(R.string.amount))
                        247 -> onFailed(context.getString(R.string.balance_failed_title))
                        243 -> onFailed(context.getString(com.urovo.urovosdklibs.R.string.charge))
                        else -> onFailed(context.getString(R.string.duplicate_receipt))
                    }
                }
                printManager.close()
            }
        } catch (e: Exception) {
            println("print exception cause->${e.cause}")
            println("print exception message->${e.message}")
            onFailed("${context.getString(R.string.balance_failed_title)} ${if (e.cause == null) "" else e.cause}")
            printManager?.close()
            e.printStackTrace()
        }
    }


private fun getBitMapBytes(bitmap: Bitmap): ByteArray? {
    val imageData: ByteArray?
    try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        imageData = byteArrayOutputStream.toByteArray()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
    return imageData
}

    override suspend fun getPrinterError(): String {
      return "ss"
    }

    override fun encrypt(data: ByteArray): ByteArray? {
return null   }

    override fun decrypt(data: ByteArray): ByteArray? {
        return null
    }

    override fun powerOnIcCard(): Boolean {
       return true
    }

    override fun powerOffIcCard() {
    }

    override fun isIcCardDetect(): Boolean {       return true

    }

    override suspend fun sendApdu(
        byteArray: ByteArray,
        onError: (String) -> Unit
    ): ByteArray? {
        return null
    }

    override suspend fun setDateTime(dataTime: String) {
    }

    override suspend fun getBatteryStatus(): Boolean {  return true
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
    }

    override suspend fun getKCv(): KCV {
        val masterResult = ByteArray(16)
        pinPad.calculateDes(
            0, 1, KeyType.MAIN_KEY, INDEX_TMK,
            HexUtils.hexStringToByte("0000000000000000"), masterResult
        )
        val dataResult = ByteArray(16)
        pinPad.calculateDes(
            0, 1, KeyType.ENCDEC_KEY, INDEX_TEK,
            HexUtils.hexStringToByte("0000000000000000"), dataResult
        )
        val pinResult = ByteArray(16)
        pinPad.calculateDes(
            0, 1, KeyType.PIN_KEY, INDEX_PIN,
            HexUtils.hexStringToByte("0000000000000000"), pinResult
        )
        val macResult = ByteArray(16)
        pinPad.calculateDes(
            0, 1, KeyType.MAC_KEY, INDEX_MAC,
            HexUtils.hexStringToByte("0000000000000000"), macResult
        )
        return KCV(
            master = HexUtils.bytesToHexString(masterResult).take(6),
            data = HexUtils.bytesToHexString(dataResult).take(6),
            pin = HexUtils.bytesToHexString(pinResult).take(6),
            mac = HexUtils.bytesToHexString(macResult).take(6)
        )
    }

    override fun getCheckValue(tt: String): ByteArray {        val macResult = ByteArray(16)

        val pinResult = ByteArray(16)
        pinPad.calculateDes(
            0, 1, KeyType.PIN_KEY, INDEX_PIN,
            HexUtils.hexStringToByte("0000000000000000"), pinResult
        )
        Log.d(TAG, "getCheckValue: ddddddd${HexUtils.bytesToHexString(pinResult)}")
      return  pinResult
    }

    override suspend fun beep(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
    }

    override suspend fun ledOn(onError: (String) -> Unit) {
    }

    override suspend fun ledOff(onError: (String) -> Unit) {
    }

    override suspend fun lockNavigationBottom(context: Context) {
    }

    private var dataCbcMode: Boolean = false







}
