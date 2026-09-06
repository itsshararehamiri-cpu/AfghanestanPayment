package com.danesh.sadad.keycard

import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.RSAPrivateKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * رمزگشایی RSA و محاسبه KCV مطابق بخش 3.3/3.4 و جدول کدهای خطای بخش 5 مستند.
 *
 * توجه مهم: مستند نوع padding مورد استفاده برای رمزنگاری کلید مستر با RSA را صریحاً مشخص
 * نکرده است. پیاده‌سازی فعلی از PKCS#1 v1.5 (رایج‌ترین روش انتقال کلید متقارن با RSA در
 * این نوع HSM/کارت‌های پرداخت) استفاده می‌کند. این فرض باید پیش از استفاده عملیاتی با یک
 * کارت واقعی سداد (یا پشتیبانی فنی سداد) تایید شود؛ در صورت نیاز [decryptTransportedKey]
 * تنها نقطه‌ای است که باید تغییر کند.
 */
internal object SadadKeyCardCrypto {

    fun buildPrivateKey(modulus: ByteArray, privateExponent: ByteArray): RSAPrivateKey {
        val n = BigInteger(1, modulus)
        val d = BigInteger(1, privateExponent)
        val spec = RSAPrivateKeySpec(n, d)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePrivate(spec) as RSAPrivateKey
    }

    fun decryptTransportedKey(privateKey: RSAPrivateKey, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encrypted)
    }

    /**
     * KCV کلید متقارن 3DES مطابق تعریف مستند: سه بایت اول حاصل رمزنگاری 8 بایت صفر با کلید.
     * کلیدهای 16 بایتی (double-length) برای DESede به 24 بایت بسط داده می‌شوند (K1|K2|K1).
     */
    fun kcvHex(key: ByteArray): String {
        val expanded = when (key.size) {
            8 -> key + key + key
            16 -> key + key.copyOfRange(0, 8)
            24 -> key
            else -> return ""
        }
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(expanded, "DESede"))
        val encryptedZeros = cipher.doFinal(ByteArray(8))
        return SadadHex.encode(encryptedZeros.copyOfRange(0, 3))
    }
}
