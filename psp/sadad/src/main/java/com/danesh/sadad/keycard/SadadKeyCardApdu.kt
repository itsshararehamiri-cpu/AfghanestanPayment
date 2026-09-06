package com.danesh.sadad.keycard

/**
 * سازنده و تفسیرگر APDU مطابق بخش 3 مستند «راهنمای استفاده از کارت کلید» (F-P102, v2.0.1).
 * تمام دستورها بر پایه‌ی جدول‌های مستند و سناریوی تست بخش 4 پیاده‌سازی شده‌اند.
 */
internal object SadadKeyCardApdu {

    /** 00 A4 04 00 0B <AppletId> */
    fun selectApplet(applet: SadadKeyCard): ByteArray {
        val appletId = applet.appletId
        return byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, appletId.size.toByte()) + appletId
    }

    /** A8 20 00 00 <len> <PIN ASCII> — رمز باید ۴ تا ۸ رقم باشد. */
    fun verifyPin(pin: String): ByteArray {
        require(pin.length in 4..8 && pin.all(Char::isDigit)) {
            "PIN must be 4-8 digits"
        }
        val pinBytes = pin.toByteArray(Charsets.US_ASCII)
        return byteArrayOf(0xA8.toByte(), 0x20, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
    }

    /** A8 B3 <RecordNumber> 00 — کلید عمومی RSA (کارت A). */
    fun readRsaPublicKey(recordNumber: Int): ByteArray =
        byteArrayOf(0xA8.toByte(), 0xB3.toByte(), recordNumber.toByte(), 0x00)

    /** A8 B4 <RecordNumber> 00 — کلید خصوصی RSA (کارت A). */
    fun readRsaPrivateExponent(recordNumber: Int): ByteArray =
        byteArrayOf(0xA8.toByte(), 0xB4.toByte(), recordNumber.toByte(), 0x00)

    /** A8 B0 <RecordNumber> <KeyNumber> — کلید مستر رمز شده با RSA (کارت B/C). */
    fun readEncryptedKey(recordNumber: Int, keyNumber: SadadKeyNumber): ByteArray =
        byteArrayOf(0xA8.toByte(), 0xB0.toByte(), recordNumber.toByte(), keyNumber.number.toByte())

    /** دو بایت پایانی پاسخ APDU را به‌عنوان SW (مثلاً 0x9000) برمی‌گرداند. */
    fun statusWord(response: ByteArray): Int {
        require(response.size >= 2) { "APDU response too short: ${response.size} bytes" }
        val sw1 = response[response.size - 2].toInt() and 0xFF
        val sw2 = response[response.size - 1].toInt() and 0xFF
        return (sw1 shl 8) or sw2
    }

    /** بدنه‌ی پاسخ APDU بدون دو بایت SW پایانی. */
    fun body(response: ByteArray): ByteArray {
        require(response.size >= 2) { "APDU response too short: ${response.size} bytes" }
        return response.copyOfRange(0, response.size - 2)
    }

    const val SW_OK = 0x9000

    /** بررسی SW و در صورت خطا پرتاب [SadadKeyCardException] با پیام قابل فهم. */
    fun requireSuccess(response: ByteArray, operation: String): ByteArray {
        val sw = statusWord(response)
        if (sw == SW_OK) return body(response)
        val error = SadadKeyCardError.fromSw(sw)
        throw SadadKeyCardException(
            message = "$operation failed: ${describe(sw, error)}",
            sw = sw,
            error = error,
        )
    }

    private fun describe(sw: Int, error: SadadKeyCardError?): String = when (error) {
        SadadKeyCardError.PUK_BLOCKED ->
            "کارت به دلیل تلاش‌های ناموفق متعدد در تایید PUK قفل شده و غیرقابل بازیابی است"
        SadadKeyCardError.PIN_BLOCKED -> "کارت به دلیل تلاش‌های ناموفق ورود رمز قفل شده است"
        SadadKeyCardError.PIN_VERIFICATION_NEEDED -> "ابتدا باید رمز کارت تایید شود"
        SadadKeyCardError.KCV_NOT_VALID -> "KCV کلید ارسالی معتبر نیست"
        SadadKeyCardError.KEY_SLOT_INITIALIZED -> "این اسلات کلید قبلاً مقداردهی شده و قابل بازنویسی نیست"
        SadadKeyCardError.KEY_SLOT_NOT_INITIALIZED -> "این اسلات کلید هنوز مقداردهی نشده است"
        SadadKeyCardError.KEY_INDEX_NOT_AVAILABLE -> "این اندیس کلید روی کارت موجود نیست"
        SadadKeyCardError.KEY_STORE_FULL -> "فضای ذخیره‌سازی کلید روی کارت پر است"
        SadadKeyCardError.LOG -> "پاسخ لاگ فعالیت کارت"
        null -> "SW=${"%04X".format(sw)}"
    }
}
