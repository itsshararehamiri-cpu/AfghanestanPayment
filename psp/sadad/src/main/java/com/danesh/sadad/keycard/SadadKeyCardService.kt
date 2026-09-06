package com.danesh.sadad.keycard

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
    ): Result<SadadKeyCardKcvSummary> = runCatching {
        require(card != SadadKeyCard.CARD_A) { "این عملیات فقط برای کارت B یا C است" }
        val stored = storage.load(keyIndex)
            ?: error("ابتدا باید کارت A با همین اندیس کلید ($keyIndex) خوانده شود")
        val privateKey = SadadKeyCardCrypto.buildPrivateKey(stored.modulus, stored.privateExponent)

        val decrypted = withCard {
            reader.selectApplet(card)
            verifyPinOrThrow(pin)
            SadadKeyNumber.entries.associateWith { keyNumber ->
                val encrypted = reader.readEncryptedKey(keyIndex, keyNumber)
                runCatching { SadadKeyCardCrypto.decryptTransportedKey(privateKey, encrypted) }
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

        injector.inject(masterKeys)
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
        val result = reader.verifyPin(pin)
        if (!result.success) throw SadadPinRejectedException(result.remainingTries)
    }
}
