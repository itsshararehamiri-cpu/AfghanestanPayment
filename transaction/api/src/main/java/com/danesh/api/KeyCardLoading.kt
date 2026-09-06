package com.danesh.api

/**
 * انتزاع UI-agnostic روال کلیدگذاری با کارت هوشمند (کارت فیزیکی، نه بلیط/کلید ثابت).
 *
 * فقط برای PSPهایی معنا دارد که [InitialConfigurationPolicy.usesKeyCardLoading]=true — در
 * حال حاضر سداد، مطابق مستند «راهنمای استفاده از کارت کلید» (F-P102). پیاده‌سازی واقعی در
 * ماژول PSP مربوطه قرار دارد تا لایه UI (feature:settings) به هیچ ماژول PSP خاصی وابسته نشود.
 */
enum class KeyCardType { CARD_A, CARD_B, CARD_C }

/** رمز کارت اشتباه بود — UI باید تعداد تلاش باقیمانده را به کاربر نشان دهد. */
class KeyCardPinRejectedException(val remainingTries: Int) :
    Exception("Incorrect key card PIN, remaining tries: $remainingTries")

data class KeyCardKcvSummary(
    val terminalMasterKey: String,
    val mac: String,
    val data: String,
    val pin: String,
)

interface KeyCardLoadingService {

    /** آیا برای این اندیس کلید، جفت کلید RSA کارت A قبلاً خوانده و ذخیره شده است. */
    suspend fun hasStoredKeyPair(keyIndex: Int): Boolean

    /** مرحله ۱: خواندن و ذخیره‌ی امن جفت کلید RSA از کارت A. */
    suspend fun loadKeyPairFromCardA(pin: String, keyIndex: Int): Result<Unit>

    /** مرحله ۲: خواندن، رمزگشایی و تزریق کلیدهای کاری از کارت B یا C. */
    suspend fun loadAndInjectMasterKeys(
        card: KeyCardType,
        pin: String,
        keyIndex: Int,
    ): Result<KeyCardKcvSummary>
}

/**
 * پیاده‌سازی پیش‌فرض برای PSPهایی که کلیدگذاری با کارت هوشمند ندارند
 * ([InitialConfigurationPolicy.usesKeyCardLoading]=false، یعنی همه به‌جز سداد در حال حاضر).
 * فقط برای این‌که گراف Hilt در فلیورهای غیر از سداد کامل بماند لازم است؛ UI هرگز این مسیر
 * را برای آن PSPها فرا نمی‌خواند.
 */
object UnsupportedKeyCardLoadingService : KeyCardLoadingService {
    override suspend fun hasStoredKeyPair(keyIndex: Int): Boolean = false

    override suspend fun loadKeyPairFromCardA(pin: String, keyIndex: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException("Key card loading is not supported for this PSP"))

    override suspend fun loadAndInjectMasterKeys(
        card: KeyCardType,
        pin: String,
        keyIndex: Int,
    ): Result<KeyCardKcvSummary> =
        Result.failure(UnsupportedOperationException("Key card loading is not supported for this PSP"))
}
