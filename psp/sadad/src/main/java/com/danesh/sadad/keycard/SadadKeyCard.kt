package com.danesh.sadad.keycard

/**
 * کارت‌های کلید سداد (مستند «راهنمای استفاده از کارت کلید» F-P102, نسخه 2.0.1).
 *
 * کارت A فقط یک بار — در اتاق امن سداد — برای تحویل جفت کلید RSA به پوز استفاده می‌شود.
 * کارت B/C توسط پشتیبان، مستقل از کارت A و در زمان نصب/به‌روزرسانی، برای تحویل کلیدهای
 * کاری رمزشده با همان جفت RSA استفاده می‌شوند.
 */
enum class SadadKeyCard(val appletIdHex: String) {
    CARD_A("53414441444B4D53410101"),
    CARD_B("53414441444B4D53420202"),
    CARD_C("53414441444B4D53430303"),
    ;

    val appletId: ByteArray get() = SadadHex.decode(appletIdHex)
}

/**
 * شماره کلیدهای موجود در هر رکورد کارت B/C، مطابق جدول بخش 3.4 مستند (01 تا 06).
 *
 * ⚠️ ناسازگاری در مستند: سناریوی تست بخش 4.2 دقیقاً همین دستور (A8 B0 <رکورد> <شماره‌کلید>)
 * را با شماره‌های 00 تا 05 (نه 01 تا 06) روی یک کارت آزمایشی اجرا کرده است. ساختار بایت
 * دستور (که در تست‌های واحد این ماژول تایید شده) صرف‌نظر از این مسئله درست است، اما اینکه
 * شماره‌ی واقعی روی کارت‌های عملیاتی سداد از 01 شروع می‌شود یا از 00، باید پیش از استفاده‌ی
 * عملیاتی با یک کارت واقعی/پشتیبانی فنی سداد تایید شود. مقادیر فعلی از جدول نام‌گذاری
 * (بخش 3.4) گرفته شده‌اند چون آن بخش صریحاً به‌عنوان تعریف رسمی نگاشت نام↔شماره ارائه شده است.
 *
 * نگاشت به اسلات‌های دستگاه در [SadadKeyCardInjector] مستند شده است.
 */
enum class SadadKeyNumber(val number: Int) {
    TERMINAL_MASTER_KEY(0x01),
    MAC(0x02),
    DATA(0x03),
    INIT_PIN(0x04),
    INIT_MAC(0x05),
    INIT_DATA(0x06),
}

/** کدهای خطای بازگشتی از کارت (بخش 5 مستند). */
enum class SadadKeyCardError(val sw: Int) {
    PUK_BLOCKED(0x63A0),
    PIN_BLOCKED(0x63A1),
    PIN_VERIFICATION_NEEDED(0x63A2),
    KCV_NOT_VALID(0x63A3),
    KEY_SLOT_INITIALIZED(0x63A4),
    KEY_SLOT_NOT_INITIALIZED(0x63A5),
    KEY_INDEX_NOT_AVAILABLE(0x63A6),
    KEY_STORE_FULL(0x63A7),
    LOG(0x63FF),
    ;

    companion object {
        fun fromSw(sw: Int): SadadKeyCardError? = entries.find { it.sw == sw }
    }
}

class SadadKeyCardException(
    message: String,
    val sw: Int? = null,
    val error: SadadKeyCardError? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

class SadadPinRejectedException(val remainingTries: Int) :
    Exception("رمز کارت نادرست است — تعداد تلاش باقیمانده: $remainingTries")

internal object SadadHex {
    fun decode(hex: String): ByteArray {
        val clean = hex.trim()
        require(clean.length % 2 == 0) { "Invalid hex length: $clean" }
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    fun encode(bytes: ByteArray): String =
        bytes.joinToString(separator = "") { "%02X".format(it) }
}
