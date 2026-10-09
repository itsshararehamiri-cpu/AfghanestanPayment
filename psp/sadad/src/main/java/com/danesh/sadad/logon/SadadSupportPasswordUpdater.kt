package com.danesh.sadad.logon

import android.util.Log
import com.danesh.common.security.SupportPasswordStore
import com.danesh.core.SensitiveBytes
import com.danesh.sadad.keycard.SadadKeyCardCrypto
import com.danesh.sadad.keycard.SadadWrappingKeyHolder
import com.danesh.sadad.util.FunctionCodeData
import com.danesh.sadad.util.IranSystemEncoding
import com.danesh.sadad.util.SadadField63Wire
import com.danesh.sadad.voucher.SadadChargePinCipher
import javax.inject.Inject
import javax.inject.Singleton

/** نتیجهٔ به‌روزرسانی رمز پشتیبان از LOGON (برای لاگ/تست). */
sealed interface SupportPasswordUpdate {
    /** سوئیچ رمز را فرستاد و جایگزین شد. */
    data object Replaced : SupportPasswordUpdate

    /** FC 001 نیامد یا خالی بود → رمز پیش‌فرض (معکوس ساعت). */
    data object ResetToDefault : SupportPasswordUpdate

    /** رمزگشایی ممکن نبود؛ رمز قبلی دست نخورد. */
    data class Failed(val reason: String) : SupportPasswordUpdate
}

/**
 * Host Function Code 001 در پاسخ LOGON سداد: رمز منوی پشتیبان، رمزشده با کلید کاری DATA (3DES-ECB).
 *
 * - هر LOGON مقدار تازه را جایگزین می‌کند.
 * - FC 001 نیامده یا خالی → رمز پشتیبان به پیش‌فرض (معکوس ساعت) برمی‌گردد.
 * - اگر رمزگشایی معتبر نشد، رمز قبلی حفظ می‌شود و در لاگ `SUPPORT_PWD` گزارش می‌شود
 *   (اگر کلید DATA درست نبود باید کلید/روش با سداد هماهنگ شود).
 */
@Singleton
class SadadSupportPasswordUpdater @Inject constructor(
    private val wrappingKeys: SadadWrappingKeyHolder,
    private val store: SupportPasswordStore,
) {

    fun apply(blocks: List<FunctionCodeData>): SupportPasswordUpdate {
        val data = blocks.firstOrNull { it.code == FUNCTION_CODE }?.data?.trim().orEmpty()
        if (data.isEmpty()) {
            store.resetToDefault()
            Log.d(TAG, "FC001 absent/empty -> support password reset to default")
            return SupportPasswordUpdate.ResetToDefault
        }
        val result = decrypt(data)
        return result.fold(
            onSuccess = { password ->
                store.setHostPassword(password)
                Log.d(TAG, "FC001 support password replaced (len=${password.length})")
                SupportPasswordUpdate.Replaced
            },
            onFailure = { error ->
                Log.w(TAG, "FC001 decrypt failed, keeping previous support password: ${error.message}")
                SupportPasswordUpdate.Failed(error.message.orEmpty())
            },
        )
    }

    internal fun decrypt(data: String): Result<String> = runCatching {
        val raw = data.toByteArray(SadadField63Wire.CHARSET)
        val cipher = IranSystemEncoding.decodeAsciiHex(raw) ?: raw
        require(cipher.isNotEmpty() && cipher.size % 8 == 0) {
            "cipher length ${cipher.size} is not a multiple of 8"
        }
        val key = wrappingKeys.workingDataKeyOrNull()
            ?: error("no working DATA key (LOGON with CHANGE_KEY required)")
        try {
            decryptWith(cipher, key)
        } finally {
            SensitiveBytes.wipe(key)
        }
    }

    companion object {
        const val FUNCTION_CODE = "001"
        private const val TAG = "SUPPORT_PWD"
        private const val MIN_LENGTH = 4
        private const val MAX_LENGTH = 16

        internal fun decryptWith(cipher: ByteArray, dataKey: ByteArray): String {
            val plain = SadadKeyCardCrypto.decrypt3DesEcb(cipher, dataKey)
            try {
                val password = SadadChargePinCipher.pinFromPlainBlock(plain, 0)
                require(password.length in MIN_LENGTH..MAX_LENGTH && password.all(Char::isDigit)) {
                    "decrypted value is not a numeric password (len=${password.length})"
                }
                return password
            } finally {
                SensitiveBytes.wipe(plain)
            }
        }
    }
}
