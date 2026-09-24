package com.danesh.sadad.voucher

import android.util.Log
import com.danesh.common.security.LocalSecretCipher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * رمز شارژ سداد برای ذخیره در گزارش تراکنش‌ها.
 *
 * قبلاً با `device.decrypt` (کلید دیتای PED) «رمز» می‌شد و گزارش هم دوباره `decrypt` می‌کرد؛
 * decrypt(decrypt(x)) برابر x نیست، و کلید PED هم با هر تزریق کلید/لاگان عوض می‌شود،
 * پس رمز شارژ در چاپ مجدد درست نمایش داده نمی‌شد. اکنون با Android Keystore رمز می‌شود.
 */
@Singleton
class SadadVoucherPinProtector @Inject constructor() {

    fun encryptToHex(plainPin: String): String {
        val pin = plainPin.trim()
        if (pin.isEmpty()) return ""
        val encrypted = LocalSecretCipher.encrypt(pin)
        if (encrypted == null) {
            Log.w(TAG, "keystore encryption failed; voucher PIN stored as plain text")
            return pin
        }
        val check = LocalSecretCipher.decrypt(encrypted)
        Log.d(TAG, "store keystore encrypt pin=$pin -> $encrypted; decrypt check=$check match=${check == pin}")
        return encrypted
    }

    private companion object {
        const val TAG = "sharjHoma"
    }
}
