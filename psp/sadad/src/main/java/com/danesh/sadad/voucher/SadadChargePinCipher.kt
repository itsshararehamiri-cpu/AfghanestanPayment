package com.danesh.sadad.voucher

import android.util.Log
import com.danesh.core.Device
import com.danesh.core.SensitiveBytes
import com.danesh.sadad.keycard.SadadKeyCardCrypto
import com.danesh.sadad.keycard.SadadWrappingKeyHolder
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * رمزگشایی / رمزگذاری رمز شارژ سداد (DE62).
 *
 * میزبان رمز شارژ را به‌صورت BCD (پد با F) در بلوک‌های ۸ بایتی با 3DES-ECB زیر DEK کاری
 * لاگان رمز می‌کند. رمزگشایی اول با DEK نرم‌افزاری ذخیره‌شده در لاگان انجام می‌شود و اگر
 * موجود نباشد با `device.decrypt` (اسلات DATA در PED). بعد از رمزگشایی، رمز دوباره
 * رمزگذاری و با ورودی مقایسه می‌شود تا در لاگ معلوم شود کلید/روش درست بوده یا نه.
 *
 * همهٔ مراحل با تگ [TAG] لاگ می‌شوند.
 */
@Singleton
class SadadChargePinCipher @Inject constructor(
    private val wrappingKeys: SadadWrappingKeyHolder,
    private val device: Device,
) {

    /**
     * سریال و رمز نهایی شارژ. رمز باید فقط رقم باشد: اگر رمزگشایی بلوک رمزشده رمز عددی
     * معتبر ندهد (کلید/چیدمان اشتباه)، چیدمان متن‌سادهٔ جایگزین از همان DE62 استفاده می‌شود.
     */
    fun resolve(pins: SadadChargePins): SadadChargePins {
        if (!pins.pinEncrypted) return pins
        val decrypted = decrypt(pins.pin, pins.pinLength)
        if (isValidChargePin(decrypted, pins.pinLength)) {
            Log.d(TAG, "PIN-RESOLVE decrypted pin is numeric; serial=${pins.serial}")
            return pins.copy(pin = decrypted, pinEncrypted = false, plainAlternatives = emptyList())
        }
        val alternative = pins.plainAlternatives.firstOrNull()
        if (alternative != null) {
            Log.w(
                TAG,
                "PIN-RESOLVE decrypted pin '$decrypted' is not numeric; using plain layout " +
                    "serial=${alternative.serial} pin=${alternative.pin}",
            )
            return alternative
        }
        Log.w(TAG, "PIN-RESOLVE decrypted pin '$decrypted' is not numeric and no plain layout fits")
        return pins.copy(pin = decrypted, pinEncrypted = false, plainAlternatives = emptyList())
    }

    /** رمز شارژ قابل نمایش؛ اگر [pins] رمزشده نباشد همان مقدار برمی‌گردد. */
    fun reveal(pins: SadadChargePins): String {
        if (!pins.pinEncrypted) {
            Log.d(TAG, "PIN-DEC skip; pin is plain text pin=${pins.pin}")
            return pins.pin
        }
        return decrypt(pins.pin, pins.pinLength)
    }

    fun decrypt(cipherHex: String, pinLength: Int): String {
        Log.d(TAG, "PIN-DEC 1) input cipherHex=$cipherHex pinLength=$pinLength")
        val cipher = runCatching { ISOUtil.hex2byte(cipherHex) }.getOrNull()
        if (cipher == null || cipher.isEmpty() || cipher.size % 8 != 0) {
            Log.w(TAG, "PIN-DEC FAILED: cipher is not a multiple of 8 bytes (len=${cipher?.size})")
            return cipherHex
        }

        val softwareKey = wrappingKeys.workingDataKeyOrNull()
        if (softwareKey != null) {
            try {
                Log.d(
                    TAG,
                    "PIN-DEC 2) software 3DES-ECB with working DATA key " +
                        "len=${softwareKey.size} kcv=${SadadKeyCardCrypto.kcvHex(softwareKey)}",
                )
                val plain = SadadKeyCardCrypto.decrypt3DesEcb(cipher, softwareKey)
                val pin = extractPin(plain, pinLength, source = "software")
                verifyRoundTrip(pin, cipherHex, softwareKey)
                return pin
            } catch (error: Exception) {
                Log.e(TAG, "PIN-DEC software decrypt failed: ${error.message}", error)
            } finally {
                SensitiveBytes.wipe(softwareKey)
            }
        } else {
            Log.w(
                TAG,
                "PIN-DEC 2) no software working DATA key stored " +
                    "(needs a logon with CHANGE_KEY after this update); trying device.decrypt",
            )
        }

        val devicePlain = runCatching { device.decrypt(cipher.copyOf()) }
            .onFailure { Log.e(TAG, "PIN-DEC device.decrypt threw: ${it.message}", it) }
            .getOrNull()
        if (devicePlain == null || devicePlain.isEmpty()) {
            Log.w(TAG, "PIN-DEC FAILED: device.decrypt returned empty; showing cipher hex")
            return cipherHex
        }
        val pin = extractPin(devicePlain, pinLength, source = "device")
        val reEncrypted = runCatching { device.encrypt(encodePinBlock(pin, cipher.size)) }
            .getOrNull()
        Log.d(
            TAG,
            "PIN-ENC device re-encrypt=${reEncrypted?.let { ISOUtil.hexString(it) }} " +
                "match=${reEncrypted?.let { ISOUtil.hexString(it).equals(cipherHex, true) }}",
        )
        return pin
    }

    /** رمزگذاری رمز شارژ با DEK کاری (برای تست / مقایسه در لاگ). */
    fun encrypt(pin: String, dataKey: ByteArray, blockBytes: Int = 8): String {
        val block = encodePinBlock(pin, blockBytes)
        Log.d(TAG, "PIN-ENC 1) plain pin=$pin bcdBlock=${ISOUtil.hexString(block)}")
        val encrypted = SadadKeyCardCrypto.encrypt3DesEcb(block, dataKey)
        val hex = ISOUtil.hexString(encrypted)
        Log.d(TAG, "PIN-ENC 2) cipherHex=$hex")
        return hex
    }

    private fun verifyRoundTrip(pin: String, cipherHex: String, key: ByteArray) {
        val blockBytes = cipherHex.length / 2
        val reEncrypted = runCatching { encrypt(pin, key, blockBytes) }.getOrNull()
        Log.d(
            TAG,
            "PIN-CHECK re-encrypt=$reEncrypted original=$cipherHex " +
                "match=${reEncrypted.equals(cipherHex, ignoreCase = true)}",
        )
    }

    private fun extractPin(plain: ByteArray, pinLength: Int, source: String): String {
        val plainHex = ISOUtil.hexString(plain)
        val pin = pinFromPlainBlock(plain, pinLength)
        Log.d(
            TAG,
            "PIN-DEC 3) [$source] plainHex=$plainHex ascii=${printable(plain)} " +
                "-> pin=$pin (len=${pin.length}, expected=$pinLength)",
        )
        if (pinLength > 0 && pin.length != pinLength) {
            Log.w(TAG, "PIN-DEC WARNING: pin length ${pin.length} != header pinLength $pinLength")
        }
        return pin
    }

    private fun printable(bytes: ByteArray): String =
        bytes.joinToString("") { b ->
            val c = b.toInt() and 0xFF
            if (c in 0x20..0x7E) c.toChar().toString() else "."
        }

    companion object {
        private const val TAG = "sharjHoma"

        /**
         * رمز از بلوک رمزگشایی‌شده: اگر همهٔ بایت‌ها رقم ASCII باشند همان متن، وگرنه BCD
         * (رقم‌ها در hex) با حذف پد F از ابتدا/انتها و برش به [pinLength].
         */
        internal fun pinFromPlainBlock(plain: ByteArray, pinLength: Int): String {
            val ascii = String(plain, Charsets.ISO_8859_1).trimEnd('\u0000', ' ', 'ÿ')
            val asciiLengthOk = pinLength <= 0 || ascii.length == pinLength
            if (ascii.isNotEmpty() && asciiLengthOk && ascii.all { it in '0'..'9' }) {
                return ascii
            }
            val hex = ISOUtil.hexString(plain).uppercase()
            val leadingPad = hex.startsWith("F")
            val digits = hex.trim('F')
            if (pinLength <= 0 || digits.length <= pinLength) return digits
            return if (leadingPad) digits.takeLast(pinLength) else digits.take(pinLength)
        }

        /** BCD رمز با پد F در انتها تا [blockBytes] بایت. */
        internal fun encodePinBlock(pin: String, blockBytes: Int): ByteArray {
            val padded = pin.padEnd(blockBytes * 2, 'F').take(blockBytes * 2)
            return ISOUtil.hex2byte(padded)
        }
    }
}
