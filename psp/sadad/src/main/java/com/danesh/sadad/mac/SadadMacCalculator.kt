package com.danesh.sadad.mac

import android.util.Log
//import com.danesh.bp.key.BpKeyConfig
//import com.danesh.bp.key.BpKeyMaterial
//import com.danesh.bp.key.encodeHexKey
import com.danesh.core.Device
import com.danesh.core.MacKeyType
import com.danesh.iso.SadadIsoMessage
import com.danesh.iso.IsoMessage
import com.danesh.iso.requireSadad
import com.danesh.sadad.key.SadadWorkingMacState
import com.danesh.sadad.key.encodeHexKey
import org.jpos.iso.ISOUtil
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

enum class SadadMacKeySource {
    INITIAL,
    WORKING,
}

@Singleton
class SadadMacCalculator @Inject constructor(
    private val device: Device,
    private val workingMacState: SadadWorkingMacState,
) {
    suspend fun applyTransactionMac(message: IsoMessage) {
        val sadad = message.requireSadad()
        val macInput = sadad.packForMac()
        val keySource = if (workingMacState.hasWorkingMac()) {
            Log.d("TAG", "applyTransactionMac: dmay")
            SadadMacKeySource.WORKING
        } else {
            Log.d("TAG", "applyTransactionMac: dmaya")

            SadadMacKeySource.INITIAL
        }
        val pedIndex = if (keySource == SadadMacKeySource.WORKING) {
            Log.d("TAG", "applyTransactionMac: dmayb")

            workingMacState.workingKeyIndex()
        } else {
            Log.d("TAG", "applyTransactionMac: dmayc")

            workingMacState.initMacIndex()
        }
        val iso = sadad.getIsoMessage()
        iso.dump(System.out,"PP>")
        val field48 = iso.getString(48).orEmpty()
        Log.d("TAG", "applyTransactionMac: dd$field48")
        val presentFields = (0..iso.maxField)
            .filter { iso.hasField(it) }
            .joinToString(",")
        Log.d(
            "MAC_DEBUG",
            "mti=${message.mti} " +
                "keySource=$keySource " +
                "pedIndex=$pedIndex " +
                "len=${macInput.size} " +
                "bitmap=${sadad.primaryBitmapHex(macInput)} " +
                "fields=$presentFields " +
                "f48Mode=${field48Mode(field48)} " +
                "f48=$field48 " +
                "input=${ISOUtil.hexString(macInput)}",
        )
        rememberMacInput(message, macInput)
        logMacInputFields(message, sadad, macInput)
        val mac = device.getMac(
            data = macInput,
            index = pedIndex,
        )
        Log.d("MAC_DEBUG", "mti=${message.mti} mac=${ISOUtil.hexString(mac)}")
        logPaddingTest(message, macInput, mac)
        message.mac = mac
        verifyMacInputMatchesWireBody(sadad, macInput)
    }

    private var balanceMacInput: ByteArray? = null
    private var billMacInput: ByteArray? = null

    private fun rememberMacInput(message: IsoMessage, macInput: ByteArray) {
        when {
            message.mti == "0100" && message.processingCode == "310000" ->
                balanceMacInput = macInput.copyOf()
            message.mti == "0200" && message.processingCode == "170000" ->
                billMacInput = macInput.copyOf()
        }
        val balance = balanceMacInput
        val bill = billMacInput
        if (balance == null || bill == null) return
        Log.d("MAC_DEBUG", "balance=${ISOUtil.hexString(balance)}")
        Log.d("MAC_DEBUG", "bill=${ISOUtil.hexString(bill)}")
        Log.d(
            "MAC_DEBUG",
            "balanceLen=${balance.size}, billLen=${bill.size}, delta=${bill.size - balance.size}",
        )
    }

    private fun logMacInputFields(
        message: IsoMessage,
        sadad: SadadIsoMessage,
        macInput: ByteArray,
    ) {
        val slices = sadad.macInputFieldSlices(macInput)
        var consumed = 0
        slices.forEach { slice ->
            consumed += slice.length
            Log.d(
                "MAC_DEBUG",
                "slice mti=${message.mti} ${slice.name} len=${slice.length} " +
                    "overflow=${slice.overflow} hex=${ISOUtil.hexString(slice.bytes)}",
            )
        }
        Log.d(
            "MAC_DEBUG",
            "slice-sum mti=${message.mti} consumed=$consumed macLen=${macInput.size} " +
                "mod8=${macInput.size % 8}",
        )
    }

    private fun logPaddingTest(message: IsoMessage, macInput: ByteArray, pedMac: ByteArray) {
        val ped8 = pedMac.copyOf(minOf(8, pedMac.size))
        val mod = macInput.size % 8
        val key = device.peekWorkingMacKey()
        if (key == null || key.isEmpty()) {
            Log.d(
                "MAC_TEST",
                "mti=${message.mti} len=${macInput.size} mod8=$mod software X9.19 skipped; " +
                    "working key is not in memory. logon again after this build, then retry. " +
                    "ped=${ISOUtil.hexString(ped8)}",
            )
            return
        }
        try {
            val caseA = calculateX919(key, macInput)
            val (caseBLabel, caseBData) = if (mod == 0) {
                "plus8Zero" to macInput + ByteArray(8)
            } else {
                val boundary = ByteArray(8 - mod)
                "plus${ISOUtil.hexString(boundary)}" to macInput + boundary
            }
            val caseB = calculateX919(key, caseBData)
            Log.d(
                "MAC_TEST",
                "mti=${message.mti} len=${macInput.size} mod8=$mod ped=${ISOUtil.hexString(ped8)}",
            )
            Log.d(
                "MAC_TEST",
                "caseA noExtraPad len=${macInput.size} mac=${ISOUtil.hexString(caseA)} " +
                    "matchPed=${caseA.contentEquals(ped8)}",
            )
            Log.d(
                "MAC_TEST",
                "caseB $caseBLabel len=${caseBData.size} mac=${ISOUtil.hexString(caseB)} " +
                    "matchPed=${caseB.contentEquals(ped8)}",
            )
        } catch (error: Exception) {
            Log.e("MAC_TEST", "mti=${message.mti} x919 failed: ${error.message}")
        } finally {
            key.fill(0)
        }
    }

    /** ANSI X9.19 با zero-pad. اگر طول مضرب ۸ باشد بلوک اضافه نمی‌گذارد. */
    private fun calculateX919(key: ByteArray, data: ByteArray): ByteArray {
        require(key.size == 16 || key.size == 24) {
            "X9.19 key must be 16 or 24 bytes, was ${key.size}"
        }
        val k1 = key.copyOfRange(0, 8)
        val k2 = key.copyOfRange(8, 16)
        val k3 = if (key.size >= 24) key.copyOfRange(16, 24) else key.copyOfRange(0, 8)
        val mod = data.size % 8
        val padded = if (mod == 0) data else data.copyOf(data.size + (8 - mod))
        var state = ByteArray(8)
        var offset = 0
        while (offset < padded.size) {
            val block = padded.copyOfRange(offset, offset + 8)
            state = desEcb(k1, xor8(state, block), encrypt = true)
            offset += 8
        }
        state = desEcb(k2, state, encrypt = false)
        return desEcb(k3, state, encrypt = true)
    }

    private fun xor8(left: ByteArray, right: ByteArray): ByteArray =
        ByteArray(8) { index -> (left[index].toInt() xor right[index].toInt()).toByte() }

    private fun desEcb(key8: ByteArray, block: ByteArray, encrypt: Boolean): ByteArray {
        val cipher = Cipher.getInstance("DES/ECB/NoPadding")
        cipher.init(
            if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
            SecretKeySpec(key8, "DES"),
        )
        return cipher.doFinal(block)
    }


    private fun verifyMacInputMatchesWireBody(message: SadadIsoMessage, macInput: ByteArray) {
        val wireBody = message.packIsoBody()
        check(wireBody.size >= SadadIsoMessage.MAC_FIELD_LENGTH) {
            "wire body کوتاه‌تر از MAC است: ${wireBody.size}"
        }
        val wireWithoutMac = wireBody.copyOf(wireBody.size - SadadIsoMessage.MAC_FIELD_LENGTH)
        val match = MessageDigest.isEqual(wireWithoutMac, macInput)
        Log.d(
            "MAC_DEBUG",
            "packForMac-check mti=${message.mti} macInputLen=${macInput.size} " +
                "wireBodyLen=${wireBody.size} wireWithoutMacLen=${wireWithoutMac.size} match=$match",
        )
        if (!match) {
            Log.e(
                "MAC_DEBUG",
                "packForMac-check macInput با بدنه wire (بدون F64) یکی نیست " +
                    "macInput=${ISOUtil.hexString(macInput)} " +
                    "wireWithoutMac=${ISOUtil.hexString(wireWithoutMac)}",
            )
        }
    }

    private fun field48Mode(field48: String): String = when {
        field48.isEmpty() -> "absent"
        field48.length == 26 && field48.all { it.isDigit() } -> "raw-26"
        else -> "other"
    }

    suspend fun applyInitMac(message: IsoMessage) {
        val pedIndex = workingMacState.initMacIndex()
        Log.d(
            "applyInitMac",
            "شروع INIT MAC pedIndex=$pedIndex mti=${message.mti}",
        )
        message.mac = calculateMac(
            message = message,
            keySource = SadadMacKeySource.INITIAL,
        )
        Log.d("applyInitMac", "فیلد 64=${message.mac?.encodeHexKey()}")
    }

    suspend fun applyLogonMac(message: IsoMessage) {
        val keySource = if (workingMacState.hasWorkingMac()) {
            SadadMacKeySource.WORKING
        } else {
            SadadMacKeySource.INITIAL
        }
        Log.d(
            "applyLogonMac",
            "شروع mti=${message.mti} keySource=$keySource " +
                "hasWorkingMac=${workingMacState.hasWorkingMac()} " +
                "pedIndex=${if (keySource == SadadMacKeySource.WORKING) workingMacState.workingKeyIndex() else workingMacState.initMacIndex()}",
        )
        val mac = calculateMac(message = message, keySource = keySource)
        message.mac = mac
        Log.d("applyLogonMac", "F64=${mac.encodeHexKey()}")
    }

    suspend fun verifyResponseMac(response: IsoMessage) {
        val bp = response.requireSadad()
        val receivedMac = response.mac
        check(receivedMac != null && receivedMac.size == SadadIsoMessage.MAC_FIELD_LENGTH) {
            "MAC پاسخ نامعتبر — فیلد 64/128 باید ۸ بایت باشد"
        }

        val macInput = bp.macInputForVerification()
        Log.d("verifyResponseMac", "mti=${response.mti} macInputLen=${macInput.size}")

        val expected = calculateInitialMac(macInput)
        Log.d("verifyResponseMac", "MAC verify با کلید اولیه software")
        check(MessageDigest.isEqual(receivedMac, expected)) {
            "اعتبارسنجی MAC پاسخ ناموفق"
        }
        Log.d("verifyResponseMac", "MAC پاسخ معتبر است (کلید اولیه)")
    }


    suspend fun verifyWorkingMac(response: IsoMessage) {
        val bp = response.requireSadad()
        val receivedMac = response.mac
        check(receivedMac != null && receivedMac.size == SadadIsoMessage.MAC_FIELD_LENGTH) {
            "MAC پاسخ نامعتبر — فیلد 64/128 باید ۸ بایت باشد"
        }

        val macIndex = workingMacState.workingKeyIndex()
        val macInput = bp.macInputForVerification()
        Log.d(
            "verifyWorkingMac",
            "mti=${response.mti} makIndex=$macIndex macInputLen=${macInput.size}",
        )
        val expected = calculateWorkingMac(macInput, macIndex)
        check(MessageDigest.isEqual(receivedMac, expected)) {
            "اعتبارسنجی MAC پاسخ ناموفق (MAK logon PED)"
        }
        Log.d(
            "verifyWorkingMac",
            "MAC پاسخ معتبر است makIndex=$macIndex expected=${expected.encodeHexKey()}",
        )
    }
//    private suspend fun injectInitialKeys(tmkIndex: Int, makIndex: Int) {
//        val initialKey = SadadKeyMaterial.initialKeyBytes()
//        Log.d(
//            "injectInitialKeys",
//            "inject Initial TMK index=$tmkIndex bytes=${initialKey.size}",
//        )
//        device.writeMasterKey(initialKey, tmkIndex)
//        Log.d(
//            "injectInitialKeys",
//            "inject Initial MAK index=$makIndex tmkIndex=$tmkIndex bytes=${initialKey.size}",
//        )
//        device.writeMacKey(initialKey, makIndex)
//        Log.d(
//            "injectInitialKeys",
//            "inject TMK index=$tmkIndex + MAK index=$makIndex انجام شد",
//        )
//    }
    suspend fun calculateMac(
        message: IsoMessage,
        keySource: SadadMacKeySource,
    ): ByteArray {
        val pedIndex = when (keySource) {
            SadadMacKeySource.INITIAL -> workingMacState.initMacIndex()
            SadadMacKeySource.WORKING -> workingMacState.workingKeyIndex()
        }
        Log.d(
            "calculateMac",
            "keySource=$keySource pedIndex=$pedIndex mti=${message.mti}",
        )
        logMessageBeforeMac(message, keySource)
        val macInput = message.requireSadad().packForMac()
        val temp = device.getMac(data = macInput, index = pedIndex)
        Log.d("TAG", "calculateMac: pedIndex=$pedIndex ->${ISOUtil.hexString(macInput)}")
        Log.d("TAG", "calculateMac: ->${ISOUtil.hexString(temp)}")
        return temp
    }

    private fun calculateInitialMacSoftware(macInput: ByteArray): ByteArray {
//        val initialKey = SadadKeyMaterial.initialMacKeyBytes()
//        return try {
//            AnsiX919Mac.calculate(initialKey, macInput)
//        } finally {
//            initialKey.fill(0)
//        }
        return ByteArray(0)
    }


    private suspend fun calculateInitialMacForPedOrSoftware(
        macInput: ByteArray,
        pedMakIndex: Int,
    ): ByteArray {
        val softwareMac = calculateInitialMacSoftware(macInput)
        Log.d(
            "calculateMac",
            "INITIAL software X9.19=${softwareMac.encodeHexKey()} pedMakIndex=$pedMakIndex",
        )
        val pedRaw = device.getMac(
            data = macInput,
            index = pedMakIndex,
            keyType = MacKeyType.WORK,
        )
        if (pedRaw.size >= SadadIsoMessage.MAC_FIELD_LENGTH) {
            val pedMac = pedRaw.copyOf(SadadIsoMessage.MAC_FIELD_LENGTH)
            val match = MessageDigest.isEqual(pedMac, softwareMac)
            Log.d(
                "calculateMac",
                "INITIAL PED=${pedMac.encodeHexKey()} matchSoftware=$match",
            )
            if (!match) {
                device.diagnoseMacMismatch(
                    data = macInput,
                    index = pedMakIndex,
                    keyType = MacKeyType.WORK,
                    referenceMac = softwareMac,
                )
            }
        } else {
            Log.d(
                "calculateMac",
                "INITIAL PED MAC unavailable — ارسال software (هاست BP)",
            )
        }
        return softwareMac
    }

    private suspend fun calculateWorkingMac(macInput: ByteArray, makIndex: Int): ByteArray {
        val pedRaw = device.getMac(
            data = macInput,
            index = makIndex,
            keyType = MacKeyType.WORK,
        )
        check(pedRaw.size >= SadadIsoMessage.MAC_FIELD_LENGTH) {
            "PED working MAK unavailable or returned an empty MAC (makIndex=$makIndex)"
        }
        Log.d(
            "calculateMac",
            "working MAC from PED only makIndex=$makIndex",
        )
        return pedRaw.copyOf(SadadIsoMessage.MAC_FIELD_LENGTH)
    }

    private fun logMessageBeforeMac(message: IsoMessage, keySource: SadadMacKeySource) {
        val bp = message.requireSadad()
        Log.d(
            "calculateMac",
            "--- dump تراکنش قبل از MAC | mti=${message.mti} keySource=$keySource ---",
        )
        when (keySource) {
            SadadMacKeySource.INITIAL -> {
                bp.printEachField("mac-pre>>", sanitized = true)
                bp.printMacInputSanitized("mac-input-view>>")
            }
            SadadMacKeySource.WORKING -> bp.printEachField("mac-pre>>", sanitized = true)
        }
        Log.d("calculateMac", "--- پایان dump تراکنش قبل از MAC ---")
    }

    private fun calculateInitialMac(macInput: ByteArray): ByteArray =
        calculateInitialMacSoftware(macInput)
}
