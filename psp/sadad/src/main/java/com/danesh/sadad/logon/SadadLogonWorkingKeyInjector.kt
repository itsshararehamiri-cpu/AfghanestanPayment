package com.danesh.sadad.logon

import android.util.Log
import com.danesh.core.Device
import com.danesh.core.SensitiveBytes
import com.danesh.sadad.key.SadadWorkingMacState
import com.danesh.sadad.key.decodeHexKey
import com.danesh.sadad.keycard.SadadKeyCardCrypto
import com.danesh.sadad.keycard.SadadWrappingKeyHolder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PIN/MAC/DATA فیلد ۴۸ لاگان به‌ترتیب زیر TMK (0x00) / MAC (0x01) / DATA (0x02) کارت رمز شده‌اند.
 * بعد از unwrap به‌عنوان working key روی اسلات C+1 PED ذخیره می‌شوند.
 */
@Singleton
class SadadLogonWorkingKeyInjector @Inject constructor(
    private val device: Device,
    private val wrappingKeys: SadadWrappingKeyHolder,
    private val workingMacState: SadadWorkingMacState,
) {
    suspend fun inject(field48: SadadLogonField48) {
        check(SadadLogonField48Parser.hasFullKeys(field48)) {
            "CHANGE_KEY NEED=1 ولی کلیدهای PIN/MAC/DATA کامل نیستند"
        }
        val cardCIndex = workingMacState.keyIndex()
        val workingIndex = cardCIndex + 1
        val pinCipher = field48.pinKey.decodeHexKey()
        val macCipher = field48.macKey.decodeHexKey()
        val dataCipher = field48.dataKey.decodeHexKey()
        val wrapTmk = wrappingKeys.requireTmk()
        val wrapMac = wrappingKeys.requireMac()
        val wrapData = wrappingKeys.requireData()
        var plainPin: ByteArray? = null
        var plainMac: ByteArray? = null
        var plainData: ByteArray? = null
        try {
            Log.d(
                "LOGON",
                "unwrap DE48 PIN under card TMK(0x00) kcv=${SadadKeyCardCrypto.kcvHex(wrapTmk)} " +
                    "MAC under MAC(0x01) DATA under DATA(0x02) at index=$cardCIndex",
            )
            plainPin = SadadKeyCardCrypto.decrypt3DesEcb(pinCipher, wrapTmk)
            plainMac = SadadKeyCardCrypto.decrypt3DesEcb(macCipher, wrapMac)
            plainData = SadadKeyCardCrypto.decrypt3DesEcb(dataCipher, wrapData)
            workingMacState.saveKeyIndices(
                cardCIndex = cardCIndex,
                rsaKeyIndex = workingMacState.rsaKeyIndex(),
            )
            Log.d(
                "LOGON",
                "store working PIN/MAC/DATA at index=$workingIndex (unwrap index=$cardCIndex)",
            )
            device.writeMasterKey(wrapTmk.copyOf(), index = cardCIndex)
            device.writePinKey(plainPin, index = workingIndex)
            device.writeMacKey(plainMac, index = workingIndex)
            device.writeDataKey(plainData, index = workingIndex)
            workingMacState.markWorkingMacLoaded()
            Log.d(
                "LOGON",
                "working keys injected pinKcv=${SadadKeyCardCrypto.kcvHex(plainPin)} " +
                    "macKcv=${SadadKeyCardCrypto.kcvHex(plainMac)} " +
                    "dataKcv=${SadadKeyCardCrypto.kcvHex(plainData)} " +
                    "workingIndex=$workingIndex initIndex=$cardCIndex",
            )
        } finally {
            SensitiveBytes.wipe(pinCipher)
            SensitiveBytes.wipe(macCipher)
            SensitiveBytes.wipe(dataCipher)
            SensitiveBytes.wipe(wrapTmk)
            SensitiveBytes.wipe(wrapMac)
            SensitiveBytes.wipe(wrapData)
            plainPin?.let { SensitiveBytes.wipe(it) }
            plainMac?.let { SensitiveBytes.wipe(it) }
            plainData?.let { SensitiveBytes.wipe(it) }
        }
    }
}
