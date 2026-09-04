package com.danesh.bp.mac

import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.key.BpKeyMaterial
import com.danesh.bp.key.encodeHexKey
import com.danesh.core.Device
import com.danesh.core.MacKeyType
import com.danesh.iso.BpIsoMessage
import com.danesh.iso.IsoMessage
import com.danesh.iso.requireBp
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

enum class BpMacKeySource {
    INITIAL,
    WORKING,
}

@Singleton
class BpMacCalculator @Inject constructor(
    private val device: Device,
) {
    suspend fun applyTransactionMac(message: IsoMessage) {
        val bp = message.requireBp()
        val macInputBefore = bp.packForMac()
        message.mac = calculateMac(message, BpMacKeySource.WORKING)
        // TODO:
       // message.mac= ByteArray(8)
        verifyMacInputMatchesWireBody(bp, macInputBefore)
        BpMacTrace.step("applyTransactionMac", "فیلد 64 توسط PED و MAK کاری تولید شد")
    }


    private fun verifyMacInputMatchesWireBody(message: BpIsoMessage, macInput: ByteArray) {
        val wireBody = message.packIsoBody()
        check(wireBody.size >= BpIsoMessage.MAC_FIELD_LENGTH) {
            "wire body کوتاه‌تر از MAC است: ${wireBody.size}"
        }
        val wireWithoutMac = wireBody.copyOf(wireBody.size - BpIsoMessage.MAC_FIELD_LENGTH)
        val match = MessageDigest.isEqual(wireWithoutMac, macInput)
        BpMacTrace.step(
            "packForMac-check",
            "mti=${message.mti} macInputLen=${macInput.size} " +
                "wireBodyLen=${wireBody.size} wireWithoutMacLen=${wireWithoutMac.size} match=$match",
        )
        if (!match) {
            BpMacTrace.error(
                "packForMac-check | macInput با بدنه wire (بدون F64) یکی نیست — " +
                    "مشکل از packForMac یا تغییر فیلد بعد از MAC",
            )
        }
    }

    suspend fun applyInitMac(message: IsoMessage) {
        BpMacTrace.step(
            "applyInitMac",
            "شروع tmkIndex= makIndex=${device.INDEX_MAC} mti=${message.mti}",
        )
        message.mac = calculateMac(
            message = message,
            keySource = BpMacKeySource.INITIAL,
            tmkIndex = device.INDEX_BOOTSTRAP_TMK,
            injectBootstrapKeys = true
        )
        BpMacTrace.step("applyInitMac", "فیلد 64=${message.mac?.encodeHexKey()}")
    }

    suspend fun applyLogonMac(message: IsoMessage) {
        val tmkIndex = device.INDEX_TMK
        val bootstrapTmk = device.INDEX_BOOTSTRAP_TMK
        val bootstrapMak = device.INDEX_BOOTSTRAP_MAC
        BpMacTrace.step(
            "applyLogonMac",
            "شروع mti=${message.mti} | طراحی BP: MAC لاگون با Initial Key روی " +
                    "bootstrap tmk=$bootstrapTmk/mak=$bootstrapMak — " +
                    "Terminal TMK index=$tmkIndex دست‌نخورده برای unwrap F62",
        )
        val mac = calculateMac(
            message = message,
            keySource = BpMacKeySource.INITIAL,
            tmkIndex = tmkIndex,
            injectBootstrapKeys = true
        )
        BpMacTrace.step(
            "applyLogonMac",
            "F64 ارسالی=${mac.encodeHexKey()} " +
                    "(مرجع هاست=AnsiX919Mac با Initial Key؛ اگر PED فرق داشت SOFTWARE ارسال شده)",
        )
        message.mac = mac
        BpMacTrace.step(
            "applyLogonMac",
            "inject نتیجه ok=true F64=${message.mac?.encodeHexKey().orEmpty()}",
        )
    }

    suspend fun verifyResponseMac(response: IsoMessage) {
        val bp = response.requireBp()
        val receivedMac = response.mac
        check(receivedMac != null && receivedMac.size == BpIsoMessage.MAC_FIELD_LENGTH) {
            "MAC پاسخ نامعتبر — فیلد 64/128 باید ۸ بایت باشد"
        }

        val macInput = bp.macInputForVerification()
        BpMacTrace.step("verifyResponseMac", "mti=${response.mti} macInputLen=${macInput.size}")

        val expected = calculateInitialMac(macInput)
        BpMacTrace.step("verifyResponseMac", "MAC verify با کلید اولیه software")
        check(MessageDigest.isEqual(receivedMac, expected)) {
            "اعتبارسنجی MAC پاسخ ناموفق"
        }
        BpMacTrace.step("verifyResponseMac", "MAC پاسخ معتبر است (کلید اولیه)")
    }


    suspend fun verifyWorkingMac(response: IsoMessage) {
        val bp = response.requireBp()
        val receivedMac = response.mac
        check(receivedMac != null && receivedMac.size == BpIsoMessage.MAC_FIELD_LENGTH) {
            "MAC پاسخ نامعتبر — فیلد 64/128 باید ۸ بایت باشد"
        }

        val macIndex = device.INDEX_MAC
        val macInput = bp.macInputForVerification()
        BpMacTrace.step(
            "verifyWorkingMac",
            "mti=${response.mti} makIndex=$macIndex macInputLen=${macInput.size}",
        )
        val expected = calculateWorkingMac(macInput, macIndex)
        check(MessageDigest.isEqual(receivedMac, expected)) {
            "اعتبارسنجی MAC پاسخ ناموفق (MAK logon PED)"
        }
        BpMacTrace.step(
            "verifyWorkingMac",
            "MAC پاسخ معتبر است makIndex=$macIndex expected=${expected.encodeHexKey()}",
        )
    }
    private suspend fun injectInitialKeys(tmkIndex: Int, makIndex: Int) {
        val initialKey = BpKeyMaterial.initialKeyBytes()
        BpMacTrace.step(
            "injectInitialKeys",
            "inject Initial TMK index=$tmkIndex bytes=${initialKey.size}",
        )
        device.writeMasterKey(initialKey, tmkIndex)
        BpMacTrace.step(
            "injectInitialKeys",
            "inject Initial MAK index=$makIndex tmkIndex=$tmkIndex bytes=${initialKey.size}",
        )
        device.writeMacKey(initialKey, makIndex)
        BpMacTrace.step(
            "injectInitialKeys",
            "inject TMK index=$tmkIndex + MAK index=$makIndex انجام شد",
        )
    }
    suspend fun calculateMac(
        message: IsoMessage,
        keySource: BpMacKeySource,
        tmkIndex: Int = device.INDEX_TMK,
        injectBootstrapKeys: Boolean = false
    ): ByteArray {
        val macIndex = device.INDEX_MAC
        BpMacTrace.step(
            "calculateMac",
            "keySource=$keySource tmkIndex=$tmkIndex macIndex=$macIndex " +
                    "injectBootstrap=$injectBootstrapKeys mti=${message.mti}",
        )
        logMessageBeforeMac(message, keySource)
        val macInput = message.requireBp().packForMac()
        BpMacTrace.step(
            "calculateMac",
            "=== قبل از تولید MAC | mti=${message.mti} keySource=$keySource " +
                    "macInputLen=${macInput.size} bytes ===",
        )
        val temp=if(injectBootstrapKeys) device.getMac(data = macInput, index = device.INDEX_BOOTSTRAP_MAC)else  device.getMac(data = macInput, index = device.INDEX_MAC)
        return temp
    }

    private fun calculateInitialMacSoftware(macInput: ByteArray): ByteArray {
        val initialKey = BpKeyMaterial.initialMacKeyBytes()
        return try {
            AnsiX919Mac.calculate(initialKey, macInput)
        } finally {
            initialKey.fill(0)
        }
    }


    private suspend fun calculateInitialMacForPedOrSoftware(
        macInput: ByteArray,
        pedMakIndex: Int,
    ): ByteArray {
        val softwareMac = calculateInitialMacSoftware(macInput)
        BpMacTrace.step(
            "calculateMac",
            "INITIAL software X9.19=${softwareMac.encodeHexKey()} pedMakIndex=$pedMakIndex",
        )
        val pedRaw = device.getMac(
            data = macInput,
            index = pedMakIndex,
            keyType = MacKeyType.WORK,
        )
        if (pedRaw.size >= BpIsoMessage.MAC_FIELD_LENGTH) {
            val pedMac = pedRaw.copyOf(BpIsoMessage.MAC_FIELD_LENGTH)
            val match = MessageDigest.isEqual(pedMac, softwareMac)
            BpMacTrace.step(
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
            BpMacTrace.step(
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
        check(pedRaw.size >= BpIsoMessage.MAC_FIELD_LENGTH) {
            "PED working MAK unavailable or returned an empty MAC (makIndex=$makIndex)"
        }
        BpMacTrace.step(
            "calculateMac",
            "working MAC from PED only makIndex=$makIndex",
        )
        return pedRaw.copyOf(BpIsoMessage.MAC_FIELD_LENGTH)
    }

    private fun logMessageBeforeMac(message: IsoMessage, keySource: BpMacKeySource) {
        val bp = message.requireBp()
        BpMacTrace.step(
            "calculateMac",
            "--- dump تراکنش قبل از MAC | mti=${message.mti} keySource=$keySource ---",
        )
        when (keySource) {
            BpMacKeySource.INITIAL -> {
                bp.printEachField("mac-pre>>", sanitized = true)
                bp.printMacInputSanitized("mac-input-view>>")
            }
            BpMacKeySource.WORKING -> bp.printEachField("mac-pre>>", sanitized = true)
        }
        BpMacTrace.step("calculateMac", "--- پایان dump تراکنش قبل از MAC ---")
    }

    private fun calculateInitialMac(macInput: ByteArray): ByteArray =
        calculateInitialMacSoftware(macInput)
}
