package com.danesh.core.emv

import java.security.SecureRandom
import java.util.Calendar

/** نوع تراکنش کهربا؛ معادل پارامتر txnType در getNFCData (Purchase = 2, Balance = 1). */
enum class KahrobaTransactionType(val code: Int) {
    BALANCE(1),
    PURCHASE(2),
    ;

    /**
     * مقدار تگ 9C دقیقاً مطابق kahroba.c: `(txnType == 1) ? 0x00 : 0x31`.
     * (در C نیز همین نگاشت در GPO، CDOL1 و فیلد ۵۵ استفاده شده است.)
     */
    val tag9C: Byte get() = if (code == 1) 0x00 else 0x31
}

/** ورودی جریان EMV کهربا. [amount] مبلغ به ریال (فقط رقم)؛ برای مانده خالی/صفر. */
data class KahrobaEmvParams(
    val transactionType: KahrobaTransactionType,
    val amount: String,
    val merchantName: String = "",
)

/** خروجی خواندن کارت کهربا (NFC) برای ارسال به سوئیچ. */
data class ContactlessCardData(
    /** Track 2 با جداکنندهٔ '=' (مثل خروجی کارت‌خوان مغناطیسی). */
    val track2: String,
    val pan: String,
    /** فیلد ۵۵ (EMV) به‌صورت HEX. */
    val iccData: String,
    /** کارت در GPO (تگ C5) درخواست PIN کرده است. */
    val cardRequestsPin: Boolean,
    /** توکن کهربا (تگ 9F61) در صورت وجود. */
    val kahrobaToken: String = "",
    val aid: String = "",
)

class KahrobaEmvException(message: String) : Exception(message)

/**
 * پیاده‌سازی Kotlin جریان EMV کهربا (پورت kahroba.c → runEMVFlow / build_emv_field55).
 *
 * مستقل از سخت‌افزار است؛ ارسال APDU از طریق [transceive] انجام می‌شود که پاسخ کامل
 * (داده + SW1 SW2) را برمی‌گرداند.
 *
 * مراحل: SELECT PPSE → SELECT AID کهربا → GPO → READ RECORD (AFL) → GENERATE AC (ARQC) → فیلد ۵۵.
 */
class KahrobaEmvKernel(
    private val transceive: (ByteArray) -> ByteArray?,
    private val log: (String) -> Unit = {},
    private val clock: () -> Calendar = { Calendar.getInstance() },
    private val random: SecureRandom = SecureRandom(),
) {

    private class Session(val params: KahrobaEmvParams) {
        val amountBcd = ByteArray(6)
        val date = ByteArray(3)
        val time = ByteArray(3)
        val unpredictableNumber = ByteArray(4)
        var aid: ByteArray = KAHROBA_AID
        var aip: ByteArray = DEFAULT_AIP
        var afl: ByteArray = ByteArray(0)
        var track2: ByteArray = ByteArray(0)
        var pan: ByteArray = ByteArray(0)
        var cdol1: ByteArray = ByteArray(0)
        var token: String = ""
        var cardRequestsPin = true
        var arqc = ByteArray(0)
        var cid = ByteArray(0)
        var atc = ByteArray(0)
        var iad = ByteArray(0)
    }

    fun run(params: KahrobaEmvParams): ContactlessCardData {
        val s = Session(params)
        selectPpse()
        selectKahrobaAid(s)
        prepareTerminalData(s)
        getProcessingOptions(s)
        readAflRecords(s)
        if (s.cdol1.isEmpty()) throw KahrobaEmvException("CDOL1 not found")
        if (s.track2.isEmpty()) throw KahrobaEmvException("Track2 (57) not found")
        generateAc(s)

        val iccData = buildField55(s)
        val track2Ascii = track2ToAscii(s.track2)
        val pan = s.pan.takeIf { it.isNotEmpty() }?.let { panToAscii(it) }
            ?: track2Ascii.substringBefore('=')
        log("Field55=${iccData.toHex()}")
        return ContactlessCardData(
            track2 = track2Ascii,
            pan = pan,
            iccData = iccData.toHex(),
            cardRequestsPin = s.cardRequestsPin,
            kahrobaToken = s.token,
            aid = s.aid.toHex(),
        )
    }

    // ------------------------------------------------------------------ APDU

    private fun exchange(name: String, apdu: ByteArray): ByteArray {
        log("$name >> ${apdu.toHex()}")
        val resp = transceive(apdu) ?: throw KahrobaEmvException("$name: no response")
        log("$name << ${resp.toHex()}")
        if (resp.size < 2) throw KahrobaEmvException("$name: short response")
        val sw1 = resp[resp.size - 2].toInt() and 0xFF
        val sw2 = resp[resp.size - 1].toInt() and 0xFF
        if (sw1 != 0x90 || sw2 != 0x00) {
            throw KahrobaEmvException("$name: SW=%02X%02X".format(sw1, sw2))
        }
        return resp.copyOfRange(0, resp.size - 2)
    }

    private fun selectPpse() {
        exchange("SELECT PPSE", select(PPSE))
    }

    private fun selectKahrobaAid(s: Session) {
        exchange("SELECT AID", select(KAHROBA_AID))
        s.aid = KAHROBA_AID
    }

    private fun prepareTerminalData(s: Session) {
        val now = clock()
        val yy = now.get(Calendar.YEAR) % 100
        s.date[0] = bcd(yy)
        s.date[1] = bcd(now.get(Calendar.MONTH) + 1)
        s.date[2] = bcd(now.get(Calendar.DAY_OF_MONTH))
        s.time[0] = bcd(now.get(Calendar.HOUR_OF_DAY))
        s.time[1] = bcd(now.get(Calendar.MINUTE))
        s.time[2] = bcd(now.get(Calendar.SECOND))
        amountToBcd(s.params.amount).copyInto(s.amountBcd)
        random.nextBytes(s.unpredictableNumber)
    }

    /** GPO با PDOL ثابت کهربا (sendTTPI در C). */
    private fun getProcessingOptions(s: Session) {
        val pdol = byteArrayOf(0x34, 0x00, 0x40, 0x02, 0x88.toByte()) +
            s.amountBcd +
            CURRENCY_CODE +
            COUNTRY_CODE +
            byteArrayOf(TERMINAL_TYPE, s.params.transactionType.tag9C)
        val data = byteArrayOf(0x83.toByte(), pdol.size.toByte()) + pdol
        val apdu = byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00, 0x00, data.size.toByte()) + data + LE
        val resp = exchange("GPO", apdu)

        // تشخیص نیاز به PIN (تگ C5): مقدار 0x85 یعنی PIN لازم نیست.
        val c5 = TlvParser.find(resp, 0xC5) ?: TlvParser.scan(resp, 0xC5)
        s.cardRequestsPin = c5 == null || c5.isEmpty() || (c5[0].toInt() and 0xFF) != 0x85
        log("GPO cvm C5=${c5?.toHex()} pinRequired=${s.cardRequestsPin}")

        if (resp.isNotEmpty() && (resp[0].toInt() and 0xFF) == 0x80) {
            // Format 1: 80 L AIP(2) AFL(n)
            val body = TlvParser.find(resp, 0x80) ?: resp.copyOfRange(2, resp.size)
            if (body.size >= 2) {
                s.aip = body.copyOfRange(0, 2)
                s.afl = body.copyOfRange(2, body.size)
            }
        } else {
            s.aip = TlvParser.find(resp, 0x82) ?: TlvParser.scan(resp, 0x82) ?: DEFAULT_AIP
            s.afl = TlvParser.find(resp, 0x94) ?: TlvParser.scan(resp, 0x94) ?: ByteArray(0)
        }
        log("AIP=${s.aip.toHex()} AFL=${s.afl.toHex()}")
        if (s.afl.isEmpty()) throw KahrobaEmvException("AFL not found")
    }

    private fun readAflRecords(s: Session) {
        var offset = 0
        while (offset + 4 <= s.afl.size) {
            val sfi = (s.afl[offset].toInt() and 0xFF) shr 3
            val first = s.afl[offset + 1].toInt() and 0xFF
            val last = s.afl[offset + 2].toInt() and 0xFF
            for (record in first..last) {
                val apdu = byteArrayOf(0x00, 0xB2.toByte(), record.toByte(), ((sfi shl 3) or 0x04).toByte(), 0x00)
                val rec = runCatching { exchange("READ RECORD sfi=$sfi rec=$record", apdu) }
                    .onFailure { log("read record failed: ${it.message}") }
                    .getOrNull() ?: continue
                val tlvs = TlvParser.parse(rec)

                val token = TlvParser.find(tlvs, 0x9F61) ?: TlvParser.scan(rec, 0x9F61)
                if (token != null && token.isNotEmpty() && s.token.isEmpty()) {
                    s.token = token.toHex().trimStart('0')
                }
                if (s.track2.isEmpty()) {
                    TlvParser.find(tlvs, 0x57)?.let { s.track2 = it }
                }
                if (s.pan.isEmpty()) {
                    TlvParser.find(tlvs, 0x5A)?.let { s.pan = it }
                }
                if (s.cdol1.isEmpty()) {
                    TlvParser.find(tlvs, 0x8C)?.let { s.cdol1 = it }
                }
            }
            offset += 4
        }
    }

    private fun generateAc(s: Session) {
        val data = java.io.ByteArrayOutputStream()
        for ((tag, len) in TlvParser.parseDol(s.cdol1)) {
            data.write(fit(cdolValue(s, tag, len), len))
        }
        val body = data.toByteArray()
        // P1 = 0x80 → ARQC
        val apdu = byteArrayOf(0x80.toByte(), 0xAE.toByte(), 0x80.toByte(), 0x00, body.size.toByte()) + body + LE
        val resp = exchange("GENERATE AC", apdu)

        if (resp.isNotEmpty() && (resp[0].toInt() and 0xFF) == 0x80) {
            // Format 1: 80 L CID(1) ATC(2) AC(8) IAD(n)
            val v = TlvParser.find(resp, 0x80) ?: ByteArray(0)
            if (v.size >= 11) {
                s.cid = v.copyOfRange(0, 1)
                s.atc = v.copyOfRange(1, 3)
                s.arqc = v.copyOfRange(3, 11)
                s.iad = v.copyOfRange(11, v.size)
            }
        } else {
            val tlvs = TlvParser.parse(resp)
            s.arqc = TlvParser.find(tlvs, 0x9F26) ?: ByteArray(0)
            s.cid = TlvParser.find(tlvs, 0x9F27) ?: ByteArray(0)
            s.atc = TlvParser.find(tlvs, 0x9F36) ?: ByteArray(0)
            s.iad = TlvParser.find(tlvs, 0x9F10) ?: ByteArray(0)
        }
        log("ARQC=${s.arqc.toHex()} CID=${s.cid.toHex()} ATC=${s.atc.toHex()} IAD=${s.iad.toHex()}")
        if (s.arqc.isEmpty() || s.arqc.all { it.toInt() == 0 }) {
            throw KahrobaEmvException("invalid ARQC")
        }
    }

    /** مقادیر CDOL1 مطابق prepareGenerateACApdu در C؛ تگ ناشناخته با صفر پر می‌شود. */
    private fun cdolValue(s: Session, tag: Int, len: Int): ByteArray = when (tag) {
        0x9F02 -> s.amountBcd
        0x9F03 -> ByteArray(6)
        0x9F1A -> COUNTRY_CODE
        0x95 -> TVR
        0x5F2A -> CURRENCY_CODE
        0x9A -> s.date
        0x9C -> byteArrayOf(s.params.transactionType.tag9C)
        0x9F37 -> s.unpredictableNumber
        0x9F35 -> byteArrayOf(TERMINAL_TYPE)
        0x9F34 -> CVM_RESULTS
        0x9F21 -> s.time
        0x9F4E -> s.params.merchantName.toByteArray(Charsets.US_ASCII).let { name ->
            ByteArray(len) { i -> if (i < name.size) name[i] else 0x20 }
        }
        else -> {
            log("Unknown CDOL1 tag: %X".format(tag))
            ByteArray(len)
        }
    }

    /** فیلد ۵۵ به همان ترتیب build_emv_field55 در C. */
    private fun buildField55(s: Session): ByteArray = TlvBuilder()
        .add(0x5F2A, CURRENCY_CODE)
        .add(0x82, s.aip)
        .add(0x95, TVR)
        .add(0x9A, s.date)
        .add(0x9C, byteArrayOf(s.params.transactionType.tag9C))
        .add(0x9F02, s.amountBcd)
        .add(0x9F03, ByteArray(6))
        .add(0x9F10, s.iad)
        .add(0x9F1A, COUNTRY_CODE)
        .add(0x9F26, s.arqc)
        .add(0x9F27, s.cid)
        .add(0x9F36, s.atc)
        .add(0x9F37, s.unpredictableNumber)
        .add(0xDF70, byteArrayOf(0x00))
        .add(0x84, s.aid)
        .add(0x9F34, CVM_RESULTS)
        .add(0x9F6E, FORM_FACTOR)
        .add(0x57, s.track2)
        .add(0x5A, s.pan)
        .build()

    companion object {
        /** AID کهربا: A0 00 00 08 03 00 01 */
        val KAHROBA_AID = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x08, 0x03, 0x00, 0x01)

        /** "2PAY.SYS.DDF01" */
        val PPSE = "2PAY.SYS.DDF01".toByteArray(Charsets.US_ASCII)

        val CURRENCY_CODE = byteArrayOf(0x09, 0x78)
        val COUNTRY_CODE = byteArrayOf(0x03, 0x64)
        val TVR = byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x00, 0x00)
        val CVM_RESULTS = byteArrayOf(0x24, 0x00, 0x02)
        val FORM_FACTOR = byteArrayOf(0x10, 0x30, 0x00, 0x00)
        val DEFAULT_AIP = byteArrayOf(0x08, 0x00)
        const val TERMINAL_TYPE: Byte = 0x21
        private const val LE: Byte = 0x00

        fun select(aid: ByteArray): ByteArray =
            byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid + LE

        /** مبلغ ریالی → BCD شش‌بایتی (n12)، معادل getBcdAmount. */
        fun amountToBcd(amount: String): ByteArray {
            val digits = amount.mapNotNull { c -> Character.digit(c, 10).takeIf { it >= 0 } }
                .joinToString("").trimStart('0').ifEmpty { "0" }
                .padStart(12, '0').takeLast(12)
            return ByteArray(6) { i ->
                (((digits[i * 2] - '0') shl 4) or (digits[i * 2 + 1] - '0')).toByte()
            }
        }

        /**
         * Track2 تگ 57 (BCD، جداکننده D، پرکنندهٔ F) → رشتهٔ ASCII با جداکنندهٔ '='.
         */
        fun track2ToAscii(track2: ByteArray): String = buildString {
            for (b in track2) {
                for (nibble in intArrayOf((b.toInt() shr 4) and 0x0F, b.toInt() and 0x0F)) {
                    when {
                        nibble <= 9 -> append('0' + nibble)
                        nibble == 0x0D -> append('=')
                        nibble == 0x0F -> return@buildString
                    }
                }
            }
        }

        /** PAN تگ 5A (BCD با پرکنندهٔ F). */
        fun panToAscii(pan: ByteArray): String = pan.toHex().trimEnd('F', 'f')

        private fun bcd(value: Int): Byte = (((value / 10) shl 4) or (value % 10)).toByte()

        private fun fit(value: ByteArray, len: Int): ByteArray = when {
            value.size == len -> value
            value.size > len -> value.copyOfRange(0, len)
            else -> value + ByteArray(len - value.size)
        }
    }
}
