package com.danesh.sadad.iso

import android.os.Build
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.common.app.AppVersionProvider
import com.danesh.core.Device
import com.danesh.iso.ByteUtil
import com.danesh.iso.IsoMessage
import com.danesh.sadad.bill.SadadBillFields
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.key.SadadWorkingMacState
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadIsoMessageSupport @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val device: Device,
    private val appVersionProvider: AppVersionProvider,
    private val workingMacState: SadadWorkingMacState,
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

    fun nextStan(): String ="${contextProvider.nextStan().toInt()+4100}"

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
//    private fun softwareVersionField(versionName: String, versionCode: Int): String {
//        val digits = digitsOnly(versionName) + digitsOnly(versionCode.toString())
//        return digits.take(SOFTWARE_VERSION_LENGTH).padEnd(SOFTWARE_VERSION_LENGTH, '0')
//    }
    /**
     * DE59 پیام INIT — طبق صفحه ۱۹ مستند PosTrans-Final.pdf (ر.ک. [SadadKeyConfig]).
     */
    fun initTransportData(): String {
        val config = contextProvider.getTerminalConfig()
        val serial = config.deviceSerial.ifBlank { device.getSerial() }
            .ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
        val serialLength = serial.length.coerceAtMost(99)
        val truncatedSerial = serial.take(serialLength)
      //  logBuildFields(appVersionProvider.versionName())
      //  logDeviceVersionInfo()
        val hw ="00016"// numericField(Build.MODEL.orEmpty(), HARDWARE_VERSION_LENGTH)
        val sw ="010203"
            /*softwareVersionField(
            appVersionProvider.versionName(),
            appVersionProvider.versionCode(),
        )*/
        val firmwareRaw = "040506"//device.getFirmwareVersion()
        val fw ="040506"// numericField(firmwareRaw, FIRMWARE_VERSION_LENGTH)
     //   Log.d(BUILD_LOG_TAG, "DE59 hw=$hw sw=$sw fw=$fw firmware1902=$firmwareRaw serial=$truncatedSerial")

        return buildString {
            append(SadadKeyConfig.INIT_STRUCTURE_VERSION)
            append(SadadKeyConfig.INIT_CONNECTION_ATTEMPTS)
            append(SadadKeyConfig.INIT_LAST_TIME_DONE)
            append(hw)
            append(sw)
            append(fw)
            append(serialLength.toString().padStart(2, '0'))
            append(truncatedSerial)
            append(workingMacState.masterKeyIndexForDe59())
            append(SadadKeyConfig.INIT_RESERVE)
            append(SadadKeyConfig.INIT_ENC_METHOD)
        }
    }
    private fun logBuildFields(appVersion: String) {
        fun log(name: String, value: String?) {
         //   Log.d(BUILD_LOG_TAG, "$name=${value.orEmpty()}")
        }
        log("APP_VERSION", appVersion)
        log("BOARD", Build.BOARD)
        log("BOOTLOADER", Build.BOOTLOADER)
        log("BRAND", Build.BRAND)
        log("DEVICE", Build.DEVICE)
        log("DISPLAY", Build.DISPLAY)
        log("FINGERPRINT", Build.FINGERPRINT)
        log("HARDWARE", Build.HARDWARE)
        log("HOST", Build.HOST)
        log("ID", Build.ID)
        log("MANUFACTURER", Build.MANUFACTURER)
        log("MODEL", Build.MODEL)
        log("PRODUCT", Build.PRODUCT)
        log("TAGS", Build.TAGS)
        log("TYPE", Build.TYPE)
        log("USER", Build.USER)
        log("RADIO", runCatching { Build.getRadioVersion() }.getOrNull())
        log("SERIAL", runCatching { @Suppress("DEPRECATION") Build.SERIAL }.getOrNull())
        log("SUPPORTED_ABIS", Build.SUPPORTED_ABIS.joinToString())
        log("TIME", Build.TIME.toString())
        log("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL)
        log("VERSION.RELEASE", Build.VERSION.RELEASE)
        log("VERSION.SDK_INT", Build.VERSION.SDK_INT.toString())
        log("VERSION.CODENAME", Build.VERSION.CODENAME)
        log("VERSION.BASE_OS", Build.VERSION.BASE_OS)
        log("VERSION.SECURITY_PATCH", Build.VERSION.SECURITY_PATCH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            log("VERSION.RELEASE_OR_CODENAME", Build.VERSION.RELEASE_OR_CODENAME)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            log("SOC_MANUFACTURER", Build.SOC_MANUFACTURER)
            log("SOC_MODEL", Build.SOC_MODEL)
            log("SKU", Build.SKU)
            log("ODM_SKU", Build.ODM_SKU)
        }
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
    fun billPaymentField48(billId: String, paymentId: String): String =
        SadadBillFields.field48(billId, paymentId)

    /** DE61 Mode 1: Mode=01 + merchant slot n2. */
    fun multiMerchantModeOne(): String =
        SadadKeyConfig.PURCHASE_DE61_MODE_ONE + SadadKeyConfig.PURCHASE_DEFAULT_MERCHANT_SLOT

    /**
     * فیلد ۶۳ (Private4) با Function Code 040 — طبق مقدمه‌ی مستند پروتکل، برای تراکنش‌های
     * شاپرک۲ (پیام‌های 0100/0200/0400) که مشخصه‌ی دیگری برای فیلد ۶۳ ندارند («Check attention»)
     * الزامی است. چون نوع اتصال واقعی ترمینال (LAN/Dialup/Portable) در پیکربندی فعلی مشخص
     * نیست، طبق خود مستند («اگر سیم‌کارت ندارد یا داده‌ای برای ارسال نیست فقط ۰۱۰۴۰۰۰۰ بفرستید»)
     * مقدار امنِ Portable بدون داده فرستاده می‌شود.
     */
    fun functionCode040Field63(): String = Field63Generator.generate(
        listOf(FunctionCodeData(SadadKeyConfig.FUNCTION_CODE_CONNECTION, "")),
    )
}
