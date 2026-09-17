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
 * سمت چپ پر شده و کلید واقعی (۱۶ بایت) در انتهای بلوک ۱۲۸ بایتی (آفست ۱۱۲) قرار دارد —
 * دقیقاً همان قراردادی که در پیاده‌سازی مرجع دستگاه دیگر (خواندن با RSA_private_decrypt و
 * برداشتن buf+7*16) دیده می‌شود. [decryptTransportedKey] این پدینگ چپ را خودش تا ۱۲۸ بایت
 * بازسازی می‌کند (چون طول آرایه‌ی خروجی Cipher با NoPadding به Provider وابسته است و ممکن
 * است صفرهای ابتدایی را از قبل حذف کرده باشد) و سپس ۱۶ بایت آخر را برمی‌دارد؛ این روش برخلاف
 * حذف ساده‌ی صفرهای ابتدایی، حتی وقتی بایت اول خودِ کلید واقعی صفر باشد هم درست کار می‌کند.
 */
internal object SadadKeyCardCrypto {

    private const val RSA_BLOCK_LEN = 128 // اندازه Modulus کارت (RSA 1024-bit)، مطابق بخش 3.3/3.4 مستند
    private const val TRANSPORTED_KEY_LEN = 16 // طول هر کلید کاری منتقل‌شده در بلوک (کلید DES دوطول)

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

        val padded = when {
            raw.size >= RSA_BLOCK_LEN -> raw.copyOfRange(raw.size - RSA_BLOCK_LEN, raw.size)
            else -> ByteArray(RSA_BLOCK_LEN - raw.size) + raw
        }
        return padded.copyOfRange(RSA_BLOCK_LEN - TRANSPORTED_KEY_LEN, RSA_BLOCK_LEN)
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
