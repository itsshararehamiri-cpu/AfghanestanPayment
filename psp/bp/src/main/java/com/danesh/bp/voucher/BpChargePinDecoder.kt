package com.danesh.bp.voucher

import android.util.Log
import com.danesh.bp.key.decodeHexKey
import com.danesh.core.Device
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpChargePinDecoder @Inject constructor(
    private val device: Device,
) {

    fun decode(encryptedTagValue: String): String {
        val encryptedHex = encryptedTagValue.trim()
        if (encryptedHex.isEmpty()) return ""

        val cipherBytes = runCatching { encryptedHex.decodeHexKey() }
            .getOrElse { error ->
                Log.w(TAG, "Charge PIN tag 0F is not valid hex: ${error.message}")
                return ""
            }
        if (cipherBytes.isEmpty()) return ""

       val decrypted = device.decrypt(ISOUtil.hex2byte(encryptedTagValue))//cipherBytes
        if (decrypted == null || decrypted.isEmpty()) {
            Log.w(TAG, "Charge PIN decrypt failed (cipherLen=${cipherBytes.size})")
            return ""
        }

        return formatDecryptedChargePin(decrypted).also { pin ->
        }
    }

    companion object {
        private const val TAG = "BpChargePinDecoder"

        internal fun formatDecryptedChargePin(decrypted: ByteArray): String {
//            // 1. تبدیل بایت‌ها به رشته و حذف Null‌های احتمالی
//            val rawString = String(decrypted, Charsets.ISO_8859_1).trimEnd('\u0000').trim()
//
//            // 2. بررسی و حذف کاراکترهای F از ابتدای رشته
//            // استفاده از regex برای حذف تمام Fهایی که در ابتدای رشته هستند
//            return rawString.replaceFirst("^F+".toRegex(), "")

            return ISOUtil.hexString(decrypted).replaceFirst("^F+".toRegex(), "")
        }
    }
}
