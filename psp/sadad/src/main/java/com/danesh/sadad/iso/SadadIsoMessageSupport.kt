package com.danesh.sadad.iso

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.ByteUtil
import com.danesh.iso.IsoMessage
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadIsoMessageSupport @Inject constructor(
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

    fun IsoMessage.applySadadStandardTerminalFields() {
        val config = contextProvider.getTerminalConfig()
        pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
        terminalId = config.terminalId.ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
        merchantId = config.merchantId.ifBlank { SadadKeyConfig.DEFAULT_MERCHANT_ID }
    }

    fun IsoMessage.applySadadFunctionCode(profile: TransactionIsoProfile) {
        val functionCode = profile.messageNii
            ?: error("Sadad Function Code (DE24) is missing for ${profile.name}")
        nii = functionCode
    }

    fun IsoMessage.applySadadFunctionCode(functionCode: String) {
        nii = functionCode
    }

    fun IsoMessage.applySadadCardFields(
        profile: TransactionIsoProfile,
        session: Session,
        pan: String,
        amount: String,
        track2: String,
        pinBlock: String,
        includeTrack2: Boolean = true,
    ) {
        mti = profile.mti
        processingCode = profile.processingCode
        stan = nextStan()
        this.pan = resolvePan(pan, track2)
        this.amount = amount
        dateTime = session.dateTime
        applySadadStandardTerminalFields()
        applySadadFunctionCode(profile)
        currency = session.currency
        if (includeTrack2) {
            this.track2 = normalizeTrack2(track2)
        }
        if (pinBlock.isNotBlank()) {
            this.pinBlock = ISOUtil.hex2byte(pinBlock)
        }
        mac = profile.emptyMac
    }

    fun IsoMessage.applySadadTransferAcquirerFields(track2: String = "") {
        val iso = getIsoMessage()
        iso.set(18, SadadKeyConfig.MERCHANT_TYPE)
        val expiry = track2.substringAfter('=', "")
            .filter { it.isDigit() }
            .take(4)
        if (expiry.length == 4) {
            iso.set(14, expiry)
        }
    }

    /**
     * DE59 Transport data — روی [IsoMessage] پراپرتی جدا ندارد؛
     * پکر HP/BP فیلد ۵۹ را IFB_LLLCHAR(999) «Transport data» تعریف کرده‌اند.
     */
    fun IsoMessage.setSadadTransportData(value: String) {
        getIsoMessage().set(59, value)
    }

    fun terminalIdOrDefault(): String {
        val config = contextProvider.getTerminalConfig()
        return config.terminalId.ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
            .padEnd(SadadKeyConfig.TERMINAL_ID_LENGTH)
            .take(SadadKeyConfig.TERMINAL_ID_LENGTH)
    }

    fun merchantIdOrDefault(): String {
        val config = contextProvider.getTerminalConfig()
        return config.merchantId.ifBlank { SadadKeyConfig.DEFAULT_MERCHANT_ID }
            .padEnd(SadadKeyConfig.MERCHANT_ID_LENGTH)
            .take(SadadKeyConfig.MERCHANT_ID_LENGTH)
    }

    fun transportData(): String {
        val config = contextProvider.getTerminalConfig()
        return config.deviceSerial.ifBlank { config.terminalId }
            .ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
    }

    /** DE48 — دادهٔ خصوصی اجباری؛ بدون تگ Function Code همراه‌پی. */
    fun additionalPrivateData(): String {
        val config = contextProvider.getTerminalConfig()
        return config.deviceSerial.ifBlank { config.terminalId }
            .ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
    }

    fun formatIsoAmount(amount: String): String {
        val digits = amount.filter(Char::isDigit)
        return digits.padStart(12, '0').takeLast(12)
    }

    /**
     * DE48 پرداخت قبض: Bill_ID ۱۳ رقم + Payment_ID ۱۳ رقم (چپ‌پد صفر).
     */
    fun billPaymentField48(billId: String, paymentId: String): String {
        val bill = billId.filter(Char::isDigit)
            .padStart(SadadKeyConfig.BILL_ID_LENGTH, '0')
            .takeLast(SadadKeyConfig.BILL_ID_LENGTH)
        val payment = paymentId.filter(Char::isDigit)
            .padStart(SadadKeyConfig.BILL_PAYMENT_ID_LENGTH, '0')
            .takeLast(SadadKeyConfig.BILL_PAYMENT_ID_LENGTH)
        return bill + payment
    }

    /** DE61 Mode 1: Mode=01 + merchant slot n2. */
    fun multiMerchantModeOne(): String =
        SadadKeyConfig.PURCHASE_DE61_MODE_ONE + SadadKeyConfig.PURCHASE_DEFAULT_MERCHANT_SLOT
}
