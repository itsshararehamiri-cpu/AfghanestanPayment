package com.danesh.sadad.keycard

import android.util.Log
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * نقطه‌ی ورود سطح بالا برای روال کلیدگذاری سداد (مطابق مستند F-P102 نسخه 2.0.1):
 *
 * مرحله ۱ — کارت A: دریافت و ذخیره‌ی امن جفت کلید RSA (یک‌بار، به ازای هر اندیس کلید).
 * مرحله ۲ — کارت B یا C: دریافت ۶ کلید رمزشده با همان RSA، رمزگشایی و تزریق به PED.
 *
 * این دو مرحله کاملاً مستقل هستند و می‌توانند با فاصله‌ی زمانی انجام شوند.
 */
@Singleton
class SadadKeyCardService @Inject constructor(
    private val reader: SadadKeyCardReader,
    private val storage: SadadKeyCardKeyPairStore,
    private val injector: SadadKeyCardInjector,
) {

    suspend fun hasStoredKeyPair(keyIndex: Int): Boolean = storage.load(keyIndex) != null

    fun isCardPresent(): Boolean = runCatching { reader.isCardPresent() }.getOrDefault(false)

    suspend fun hasApplet(card: SadadKeyCard): Boolean = runCatching {
        withCard {
            reader.selectApplet(card)
            true
        }
    }.getOrDefault(false)

    suspend fun loadKeyPairFromCardA(pin: String, keyIndex: Int): Result<Unit> = runCatching {
        withCard {
            reader.selectApplet(SadadKeyCard.CARD_A)
            verifyPinOrThrow(pin)
            val modulus = reader.readRsaPublicModulus(keyIndex)
            val exponent = reader.readRsaPrivateExponent(keyIndex)
            check(modulus.isNotEmpty() && exponent.isNotEmpty()) {
                "پاسخ کارت A حاوی کلید معتبر نبود"
            }
            storage.save(keyIndex, modulus, exponent)
        }
    }

    suspend fun loadAndInjectMasterKeys(
        card: SadadKeyCard,
        pin: String,
        keyIndex: Int,
        rsaKeyIndex: Int = keyIndex,
    ): Result<SadadKeyCardKcvSummary> = runCatching {
        Log.d("TAG", "loadAndInjectMasterKeys() calledcard$card")
        Log.d("TAG", "loadAndInjectMasterKeys() calledpin$pin")
        Log.d("TAG", "loadAndInjectMasterKeys() calledkeyindex$keyIndex rsa=$rsaKeyIndex")

        require(card != SadadKeyCard.CARD_A) { "این عملیات فقط برای کارت B یا C است" }
        val stored = storage.load(rsaKeyIndex)
            ?: error("ابتدا باید کارت A با اندیس کلید ($rsaKeyIndex) خوانده شود")
        val privateKey = SadadKeyCardCrypto.buildPrivateKey(stored.modulus, stored.privateExponent)

        val decrypted = withCard {
            reader.selectApplet(card)
            verifyPinOrThrow(pin)
            SadadKeyNumber.entries.associateWith { keyNumber ->
                Log.d("TAG", "loadAndInjectMasterkeyIndexKeys: $keyIndex")
                val encrypted = reader.readEncryptedKey(keyIndex, keyNumber)
                Log.d("TAG", "loadAndInjectMasterKeys: keyNumber$keyNumber")
                Log.d("TAG", "loadAndInjectMasterKeys: encrypted${ISOUtil.hexString(encrypted)}")

                runCatching {val twmp= SadadKeyCardCrypto.decryptTransportedKey(privateKey, encrypted)
                    Log.d("TAG", "loadAndInjectMasterKeys: decryptTransportedKey${ISOUtil.hexString(twmp)}")

                    twmp}
                    .getOrElse { cause ->
                        throw SadadKeyCardException(
                            message = "رمزگشایی کلید ${keyNumber.name} ناموفق بود — " +
                                "احتمال عدم تطابق کارت با جفت کلید RSA ذخیره‌شده",
                            cause = cause,
                        )
                    }
            }
        }

        val masterKeys = SadadKeyCardMasterKeys(
            terminalMasterKey = decrypted.getValue(SadadKeyNumber.TERMINAL_MASTER_KEY),
            macKey = decrypted.getValue(SadadKeyNumber.MAC),
            dataKey = decrypted.getValue(SadadKeyNumber.DATA),
            pinKey = decrypted.getValue(SadadKeyNumber.INIT_PIN),
            initMacKey = decrypted.getValue(SadadKeyNumber.INIT_MAC),
            initDataKey = decrypted.getValue(SadadKeyNumber.INIT_DATA),
        )

        injector.inject(masterKeys, keyIndex = keyIndex, rsaKeyIndex = rsaKeyIndex)
    }

    private suspend fun <T> withCard(block: suspend () -> T): T {
        check(reader.powerOn()) { "روشن‌سازی کارت‌خوان ناموفق بود" }
        try {
            check(reader.isCardPresent()) { "کارتی در کارت‌خوان شناسایی نشد" }
            return block()
        } finally {
            reader.powerOff()
        }
    }

    private suspend fun verifyPinOrThrow(pin: String) {
        Log.d("TAG", "verifyPinOrThrow() called with: pin = $pin")
        val result = reader.verifyPin(pin)
        Log.d("TAG", "verifyPinOrThrow() called with: pin = $result")

        if (!result.success) throw SadadPinRejectedException(result.remainingTries)
    }
}
