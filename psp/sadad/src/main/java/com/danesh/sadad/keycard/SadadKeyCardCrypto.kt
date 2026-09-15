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
 * padding: مستند صریحاً نوع padding را ذکر نکرده، اما با رمزگشایی دستی سناریوی تست بخش
 * 4 (جفت کلید واقعی کارت A + شش کلید رمزشده واقعی کارت C) تایید شد که رمزنگاری کارت،
 * RSA خام (بدون padding، یعنی «تدارک صفر» ساده تا طول Modulus) است — بلوک رمزگشایی‌شده
 * ساختار PKCS#1 v1.5 (۰۰۰۲ + بایت‌های تصادفی غیرصفر + ۰۰) ندارد، فقط با بایت‌های صفر در
 * سمت چپ پر شده و کلید واقعی در انتهای آن قرار دارد. بنابراین [decryptTransportedKey] از
 * "RSA/ECB/NoPadding" استفاده کرده و بایت‌های صفر ابتدایی را حذف می‌کند.
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
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val raw = cipher.doFinal(encrypted)
        return raw.dropWhile { it == 0.toByte() }.toByteArray()
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
