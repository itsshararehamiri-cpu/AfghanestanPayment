package com.danesh.sadad.keycard

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class SadadStoredWrappingKeys(
    val terminalMasterKey: ByteArray,
    val pinKey: ByteArray,
    val macKey: ByteArray,
    val dataKey: ByteArray,
)

class SadadWrappingKeyStore(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(keys: SadadStoredWrappingKeys) {
        prefs.edit()
            .putString(KEY_TMK, encrypt(keys.terminalMasterKey))
            .putString(KEY_PIN, encrypt(keys.pinKey))
            .putString(KEY_MAC, encrypt(keys.macKey))
            .putString(KEY_DATA, encrypt(keys.dataKey))
            .apply()
    }

    fun load(): SadadStoredWrappingKeys? {
        val tmk = prefs.getString(KEY_TMK, null) ?: return null
        val pin = prefs.getString(KEY_PIN, null) ?: return null
        val mac = prefs.getString(KEY_MAC, null) ?: return null
        val data = prefs.getString(KEY_DATA, null) ?: return null
        return SadadStoredWrappingKeys(
            terminalMasterKey = decrypt(tmk),
            pinKey = decrypt(pin),
            macKey = decrypt(mac),
            dataKey = decrypt(data),
        )
    }

    /** MAK کاری لاگان — برای MAC نرم‌افزاری پیام‌های هم‌تراز (طول مضرب ۸). */
    fun saveWorkingMac(key: ByteArray) {
        prefs.edit().putString(KEY_WORKING_MAC, encrypt(key)).apply()
    }

    fun loadWorkingMac(): ByteArray? =
        prefs.getString(KEY_WORKING_MAC, null)?.let(::decrypt)

    fun clearWorkingMac() {
        prefs.edit().remove(KEY_WORKING_MAC).apply()
    }

    /** DEK کاری لاگان — برای رمزگشایی رمز شارژ (DE62) با 3DES نرم‌افزاری. */
    fun saveWorkingData(key: ByteArray) {
        prefs.edit().putString(KEY_WORKING_DATA, encrypt(key)).apply()
    }

    fun loadWorkingData(): ByteArray? =
        prefs.getString(KEY_WORKING_DATA, null)?.let(::decrypt)

    fun clearWorkingData() {
        prefs.edit().remove(KEY_WORKING_DATA).apply()
    }

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
        if (existing != null) return existing
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

    private companion object {
        const val PREFS_NAME = "sadad_wrapping_key_prefs"
        const val KEY_TMK = "wrap_tmk"
        const val KEY_PIN = "wrap_pin"
        const val KEY_MAC = "wrap_mac"
        const val KEY_DATA = "wrap_data"
        const val KEY_WORKING_MAC = "working_mac"
        const val KEY_WORKING_DATA = "working_data"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "sadad_wrapping_kek"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
