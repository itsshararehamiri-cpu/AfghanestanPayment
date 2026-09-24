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
import com.danesh.sadad.keycard.SadadKeyCardCrypto
import com.danesh.sadad.keycard.SadadWrappingKeyHolder
import org.jpos.iso.ISOUtil
import java.security.MessageDigest
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
    private val wrappingKeys: SadadWrappingKeyHolder,
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
        val mac = computeMac(
            mti = message.mti,
            macInput = macInput,
            pedIndex = pedIndex,
            keySource = keySource,
        )
        Log.d("MAC_DEBUG", "mti=${message.mti} mac=${ISOUtil.hexString(mac)}")
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

    /**
     * PED دستگاه K9 (TYPE_X919_00) وقتی طول داده مضرب ۸ است یک بلوک صفر اضافه پد می‌کند،
     * ولی هاست سداد (ISO 9797-1 روش ۱) هیچ بلوکی اضافه نمی‌کند. به همین دلیل موجودی
     * (طول ۱۱۸) درست بود و پرداخت قبض (طول ۱۵۲) با کد 19 رد می‌شد.
     *
     * - طول غیرهم‌تراز: همان PED (رفتارش با هاست یکی است).
     * - طول هم‌تراز + کلید کاری: X9.19 نرم‌افزاری با MAK لاگان، به شرط این‌که KCV/MAC پروب
     *   با PED یکی باشد (یعنی کلید نرم‌افزار همان کلید اسلات PED است).
     * - طول هم‌تراز بدون کلید: MacType دیگری از PED که روی داده هم‌تراز بلوک اضافه نگذارد
     *   (با پروب پیدا می‌شود).
     */
    private suspend fun computeMac(
        mti: String?,
        macInput: ByteArray,
        pedIndex: Int,
        keySource: SadadMacKeySource,
    ): ByteArray {
        val pedMac = device.getMac(data = macInput, index = pedIndex)
        if (macInput.size % 8 != 0) return pedMac
        logPedPaddingSelfTest(pedIndex)
        if (keySource == SadadMacKeySource.WORKING) {
            softwareMacOrNull(mti, macInput, pedIndex)?.let { return it }
        }
        alignedPedMacOrNull(mti, macInput, pedIndex)?.let { return it }
        Log.e(
            "MAC_DEBUG",
            "mti=$mti len=${macInput.size} aligned: no software key and no PED MacType without " +
                "extra block — PED MAC sent (host will reject with 19). Do a LOGON with CHANGE_KEY.",
        )
        return pedMac
    }

    private suspend fun softwareMacOrNull(
        mti: String?,
        macInput: ByteArray,
        pedIndex: Int,
    ): ByteArray? {
        val key = wrappingKeys.workingMacKeyOrNull()
        if (key == null) {
            Log.w("MAC_DEBUG", "mti=$mti working MAC key not stored — trying PED MacType probe")
            return null
        }
        return try {
            if (!softwareKeyMatchesPed(key, pedIndex)) {
                Log.e(
                    "MAC_DEBUG",
                    "mti=$mti stored working MAC key does not match PED slot $pedIndex",
                )
                return null
            }
            val softwareMac = SadadAnsiX919Mac.calculate(key, macInput)
            Log.d(
                "MAC_DEBUG",
                "mti=$mti len=${macInput.size} aligned → software X9.19 " +
                    "mac=${ISOUtil.hexString(softwareMac)}",
            )
            softwareMac
        } finally {
            key.fill(0)
        }
    }

    /** null = هنوز پروب نشده؛ "" = هیچ نوعی جواب نداد. */
    @Volatile
    private var alignedMacType: String? = null

    /**
     * بدون کلید: مرجع درست برای داده هم‌تراز P (بایت آخر 00) همان MAC پیش‌فرض PED روی
     * P بدون بایت آخر است (غیرهم‌تراز → padding استاندارد). هر MacType دیگری که روی P
     * همین نتیجه را بدهد، روی داده هم‌تراز بلوک اضافه نمی‌گذارد.
     */
    private suspend fun alignedPedMacOrNull(
        mti: String?,
        macInput: ByteArray,
        pedIndex: Int,
    ): ByteArray? {
        val type = alignedMacType ?: probeAlignedMacType(pedIndex).also { alignedMacType = it }
        if (type.isEmpty()) return null
        val mac = device.getMacWithType(data = macInput, index = pedIndex, macType = type)
        if (mac.size < SadadAnsiX919Mac.MAC_LENGTH) return null
        Log.d(
            "MAC_DEBUG",
            "mti=$mti len=${macInput.size} aligned → PED macType=$type mac=${ISOUtil.hexString(mac)}",
        )
        return mac.copyOf(SadadAnsiX919Mac.MAC_LENGTH)
    }

    private suspend fun probeAlignedMacType(pedIndex: Int): String {
        val probes = ALIGNED_PROBES.map { probe ->
            probe to device.getMac(data = probe.copyOf(probe.size - 1), index = pedIndex)
        }
        for (type in CANDIDATE_MAC_TYPES) {
            val allMatch = probes.all { (probe, reference) ->
                val candidate = runCatching {
                    device.getMacWithType(data = probe.copyOf(), index = pedIndex, macType = type)
                }.getOrDefault(ByteArray(0))
                val ok = candidate.size >= SadadAnsiX919Mac.MAC_LENGTH &&
                    reference.size >= SadadAnsiX919Mac.MAC_LENGTH &&
                    candidate.copyOf(SadadAnsiX919Mac.MAC_LENGTH)
                        .contentEquals(reference.copyOf(SadadAnsiX919Mac.MAC_LENGTH))
                Log.d(
                    "MAC_DEBUG",
                    "aligned probe type=$type len=${probe.size} " +
                        "candidate=${ISOUtil.hexString(candidate)} " +
                        "reference=${ISOUtil.hexString(reference)} match=$ok",
                )
                ok
            }
            if (allMatch) {
                Log.d("MAC_DEBUG", "aligned probe → using PED macType=$type for aligned input")
                return type
            }
        }
        Log.e("MAC_DEBUG", "aligned probe → no PED MacType matches standard X9.19")
        return ""
    }

    @Volatile
    private var verifiedKey: Pair<Int, String>? = null

    /** مقایسه روی داده ۷ بایتی (غیرهم‌تراز) که رفتار padding PED روی آن درست است. */
    private suspend fun softwareKeyMatchesPed(key: ByteArray, pedIndex: Int): Boolean {
        val kcv = SadadKeyCardCrypto.kcvHex(key)
        if (verifiedKey == (pedIndex to kcv)) return true
        val probe = PROBE_UNALIGNED.copyOf()
        val ped = device.getMac(data = probe, index = pedIndex)
        val software = SadadAnsiX919Mac.calculate(key, probe)
        val match = ped.size >= SadadAnsiX919Mac.MAC_LENGTH &&
            MessageDigest.isEqual(ped.copyOf(SadadAnsiX919Mac.MAC_LENGTH), software)
        Log.d(
            "MAC_DEBUG",
            "working key probe pedIndex=$pedIndex kcv=$kcv ped=${ISOUtil.hexString(ped)} " +
                "software=${ISOUtil.hexString(software)} match=$match",
        )
        if (match) verifiedKey = pedIndex to kcv
        return match
    }

    @Volatile
    private var paddingSelfTestDone = false

    /**
     * بدون نیاز به کلید: اگر PED استاندارد باشد MAC(۸ بایت صفر) == MAC(۷ بایت صفر).
     * اگر فرق کند یعنی PED روی داده هم‌تراز یک بلوک اضافه پد می‌کند.
     */
    private suspend fun logPedPaddingSelfTest(pedIndex: Int) {
        if (paddingSelfTestDone) return
        paddingSelfTestDone = true
        runCatching {
            val aligned = device.getMac(data = ByteArray(8), index = pedIndex)
            val unaligned = device.getMac(data = ByteArray(7), index = pedIndex)
            Log.d(
                "MAC_DEBUG",
                "PED padding self-test mac(8x00)=${ISOUtil.hexString(aligned)} " +
                    "mac(7x00)=${ISOUtil.hexString(unaligned)} " +
                    "pedAddsExtraBlockWhenAligned=${!aligned.contentEquals(unaligned)}",
            )
        }.onFailure { error ->
            Log.e("MAC_DEBUG", "PED padding self-test failed: ${error.message}")
        }
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
        val temp = computeMac(
            mti = message.mti,
            macInput = macInput,
            pedIndex = pedIndex,
            keySource = keySource,
        )
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

    private companion object {
        val PROBE_UNALIGNED = byteArrayOf(0x53, 0x41, 0x44, 0x41, 0x44, 0x4D, 0x41)

        /** داده‌های هم‌تراز با بایت آخر 00 (۱۶ و ۲۴ بایت). */
        val ALIGNED_PROBES = listOf(
            ByteArray(16) { i -> if (i == 15) 0.toByte() else (0x31 + i).toByte() },
            ByteArray(24) { i -> if (i == 23) 0.toByte() else (0xA0 + i).toByte() },
        )

        val CANDIDATE_MAC_TYPES = listOf(
            "TYPE_X919",
            "TYPE_X919_MP",
            "TYPE_BOC_EXTENED",
            "TYPE_X9_9",
            "TYPE_XOR_3DES",
            "TYPE_CUP_ECB",
        )
    }
}
