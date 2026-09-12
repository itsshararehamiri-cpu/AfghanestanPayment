package com.danesh.hp.iso

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.ByteUtil
import com.danesh.iso.IsoMessage
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpIsoMessageSupport @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
) {
    data class Session(
        val currency: String,
        val dateTime: String,
    )

    fun beginSession(): Session {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return Session(
            currency = config.currency,
            dateTime = "${clock.date.drop(2)}${clock.time}",
        )
    }

    fun nextStan(): String = contextProvider.nextStan()

    fun merchantName(): String = contextProvider.getTerminalConfig().merchantName

    fun resolvePan(pan: String, track2: String): String {
        if (pan.isNotBlank()) return pan
        return track2.substringBefore('=').substringBefore('^').trim()
    }

    fun normalizeTrack2(raw: String): String {
        if (raw.contains('=') || raw.contains('^')) return raw
        if (raw.length % 2 != 0 || raw.length <= 37) return raw
        if (!raw.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) return raw
        return ByteUtil.hex2Str(raw)
    }

    /**
     * DE24 در همراه‌پی Function Code است و برای هر تراکنش از [profile] خوانده می‌شود.
     * (property داخلی همچنان `nii` نام دارد چون به فیلد 24 مپ می‌شود.)
     */
    fun IsoMessage.applyHpFunctionCode(profile: TransactionIsoProfile) {
        val functionCode = profile.messageNii
            ?: error("HP Function Code (DE24) is missing for ${profile.name}")
        nii = functionCode
    }

    fun IsoMessage.applyHpFunctionCodeValue(functionCode: String) {
        nii = functionCode
    }

    /**
     * DE41 / DE42 — مالی، استعلام و reverse.
     * terminalId/merchantId از SharedPreferences (از طریق [TransactionContextProvider]) خوانده
     * می‌شوند؛ اگر هنوز برای همراه‌پی سفارشی نشده باشند (خالی یا برابر پیش‌فرض عمومی موتور
     * تراکنش)، مقادیر ثابت همراه‌پی در [HpKeyConfig] به‌عنوان دیفالت به‌کار می‌روند.
     */
    fun IsoMessage.applyHpAcceptorIds(
        terminalIdValue: String = contextProvider.getTerminalConfig().terminalId
            .ifBlank { HpKeyConfig.DEFAULT_TERMINAL_ID },
        merchantIdValue: String = contextProvider.getTerminalConfig().merchantId.let {
            if (it.isBlank() || it == HpKeyConfig.ENGINE_DEFAULT_MERCHANT_ID) {
                HpKeyConfig.DEFAULT_MERCHANT_ID
            } else {
                it
            }
        },
    ) {
        terminalId = terminalIdValue
        merchantId = merchantIdValue
    }

    /**
     * DE26 — مالی، استعلام و reverse؛ DE18 مجاز نیست.
     * اگر پیکربندی ترمینال (پاسخ 1314 همراه‌پی — بخش 9.3 مستند KAREN) مقدار MCC را
     * فراهم کرده باشد از همان استفاده می‌شود؛ در غیر این صورت مقدار ثابت پیش‌فرض.
     */
    fun IsoMessage.applyHpMerchantCategory() {
        val mcc = contextProvider.getTerminalConfig().mcc.ifBlank { HpKeyConfig.MERCHANT_TYPE }
        getIsoMessage().set(26, mcc)
    }

    /** DE22 — فقط درخواست مالی کارت‌به‌کارت و کارت‌به‌کیف */
    fun IsoMessage.applyHpPosDataCode() {
        pointOfServiceEntryMode = HpKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
    }

    /** DE49 — فقط جریان انتقال و قبض */
    fun IsoMessage.applyHpTransactionCurrency(code: String) {
        currency = code.ifBlank { HpKeyConfig.CARDHOLDER_BILLING_CURRENCY }
    }

    /** DE51 — فقط انتقال کارت‌به‌کارت و کارت‌به‌کیف */
    fun IsoMessage.applyHpBillingCurrency() {
        tt51 = HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
    }

    /**
     * DE14 فقط وقتی از Track 2 قابل استخراج باشد؛ DE35 برای مالی و reverse.
     */
    fun IsoMessage.applyHpTrack2AndExpiry(rawTrack2: String) {
        val track2Value = normalizeTrack2(rawTrack2)
        if (track2Value.isBlank()) return
        track2 = track2Value
        expiryFromTrack2(track2Value)?.let { getIsoMessage().set(14, it) }
    }

    fun IsoMessage.applyHpPinBlock(pinBlockHex: String) {
        if (pinBlockHex.isBlank()) return
        pinBlock = ISOUtil.hex2byte(pinBlockHex)
    }

    /** DE64 برای MAC لایهٔ بعدی؛ DE72 هرگز در 1100/1600/1420 ست نمی‌شود. */
    fun IsoMessage.applyHpEmptyMac(profile: TransactionIsoProfile) {
        mac = profile.emptyMac
        getIsoMessage().unset(72)
    }

    companion object {
        fun expiryFromTrack2(track2: String): String? {
            val expiry = track2.substringAfter('=', "")
                .filter { it.isDigit() }
                .take(4)
            return expiry.takeIf { it.length == 4 }
        }

        fun reversalRrn(storedResponseRrn: String?, stan: String, localDateTime: String): String {
            val stored = storedResponseRrn?.trim().orEmpty()
            if (stored.isNotBlank()) return normalizeRrn(stored)
            val fromDateTime = localDateTime.filter { it.isDigit() }
            if (fromDateTime.length >= 12) return fromDateTime.takeLast(12)
            val stanDigits = stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
            return (stanDigits + fromDateTime).padEnd(12, '0').take(12)
        }

        fun normalizeRrn(rrn: String): String {
            val digits = rrn.filter { it.isDigit() }
            return if (digits.length >= 12) digits.takeLast(12) else rrn.take(12).padStart(12, '0')
        }
    }
}
