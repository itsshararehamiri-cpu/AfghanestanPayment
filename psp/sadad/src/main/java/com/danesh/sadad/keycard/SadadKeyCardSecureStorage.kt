package com.danesh.sadad.keycard

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class SadadStoredRsaKeyPair(
    val modulus: ByteArray,
    val privateExponent: ByteArray,
)

/** ذخیره‌سازی امن جفت کلید RSA کارت A، جدا شده به‌عنوان واسط برای امکان تست واحد. */
interface SadadKeyCardKeyPairStore {
    fun save(keyIndex: Int, modulus: ByteArray, privateExponent: ByteArray)
    fun load(keyIndex: Int): SadadStoredRsaKeyPair?
    fun clear(keyIndex: Int)
}

/**
 * ذخیره‌ی امن جفت کلید RSA دریافتی از کارت A، تا زمان دریافت کلید مستر از کارت B/C.
 *
 * طبق مستند، این دو مرحله کاملاً مستقل از هم و ممکن است با فاصله زمانی (نصب/به‌روزرسانی)
 * انجام شوند، بنابراین جفت کلید باید بین اجراهای برنامه هم باقی بماند. کلید خصوصی RSA با
 * یک کلید AES-GCM نگه‌داری‌شده در Android Keystore (که هرگز از سخت‌افزار امن خارج نمی‌شود)
 * رمزنگاری و در SharedPreferences ذخیره می‌شود.
 */
@Singleton
class SadadKeyCardSecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) : SadadKeyCardKeyPairStore {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun save(keyIndex: Int, modulus: ByteArray, privateExponent: ByteArray) {
        prefs.edit()
            .putString(modulusKey(keyIndex), encrypt(modulus))
            .putString(exponentKey(keyIndex), encrypt(privateExponent))
            .apply()
    }

    override fun load(keyIndex: Int): SadadStoredRsaKeyPair? {
        val modulusEncoded = prefs.getString(modulusKey(keyIndex), null) ?: return null
        val exponentEncoded = prefs.getString(exponentKey(keyIndex), null) ?: return null
        return SadadStoredRsaKeyPair(
            modulus = decrypt(modulusEncoded),
            privateExponent = decrypt(exponentEncoded),
        )
    }

    override fun clear(keyIndex: Int) {
        prefs.edit()
            .remove(modulusKey(keyIndex))
            .remove(exponentKey(keyIndex))
            .apply()
    }

    private fun modulusKey(keyIndex: Int) = "rsa_modulus_$keyIndex"
    private fun exponentKey(keyIndex: Int) = "rsa_exponent_$keyIndex"

    private fun encrypt(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain)
        return Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): ByteArray {
        val (ivPart, cipherPart) = encoded.split(":", limit = 2)
        val iv = Base64.decode(ivPart, Base64.NO_WRAP)
        val cipherText = Base64.decode(cipherPart, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(cipherText)
    }

    private fun keystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing == null) {
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE,
            )
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generator.generateKey()
            return keystoreKey()
        }
        return existing
    }

    companion object {
        private const val PREFS_NAME = "sadad_keycard_secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "sadad_keycard_rsa_kek"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
