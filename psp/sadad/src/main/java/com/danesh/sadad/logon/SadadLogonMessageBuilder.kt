package com.danesh.sadad.logon

import android.util.Log
import com.danesh.api.OptionalReceiptLimits
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.common.diagnostics.StartupTraceFile
import com.danesh.sadad.diagnostics.traceIso
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadLogonMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
    private val messageSupport: SadadIsoMessageSupport, private val macCalculator: SadadMacCalculator,

    ) {
    /**
     * @param optionalReceipt اگر مقدار داشته باشد، Client Function Code 034 (کف/سقف رسید اختیاری
     * واردشده توسط کاربر) در DE63 همین پیام 0800 ارسال می‌شود؛ سوئیچ مقدار نهایی را در FC 033 برمی‌گرداند.
     */
    suspend fun build(optionalReceipt: OptionalReceiptLimits? = null): IsoMessage {
        StartupTraceFile.line("LogonMessage", "build start")
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        // مستند: DE11 پیام INIT باید یک عدد تصادفی ۶ رقمی باشد، نه شمارنده‌ی ترتیبی nextStan().
        val stanTemp = contextProvider.nextStan() //(0..999999).random().toString().padStart(6, '0')
        val randomTerminalId = (clock.time + stanTemp.takeLast(2)).takeLast(SadadKeyConfig.TERMINAL_ID_LENGTH)

        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.LOGON_MTI
            processingCode = SadadKeyConfig.LOGON_PROCESSING_CODE
            stan = stanTemp
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            nii = SadadKeyConfig.SADAD_NII
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            transportData = messageSupport.initTransportData()
            if (optionalReceipt != null) {
                privateUseField63 = Field63Generator.generate(
                    listOf(FunctionCodeData(OPTIONAL_RECEIPT_FUNCTION_CODE, optionalReceiptData(optionalReceipt))),
                )
            }
        }
        StartupTraceFile.line("LogonMessage", "apply MAC")
        macCalculator.applyLogonMac(message)
        traceIso("LogonMessage request", message)
        StartupTraceFile.line("LogonMessage", "build done")
        return message

    }

    companion object {
        /** Client Function Code 034: کف/سقف رسید اختیاری. */
        const val OPTIONAL_RECEIPT_FUNCTION_CODE = "034"

        /** Active n1 + Lower Bound n12 + Upper Bound n12 (همان چیدمان Host FC 033). */
        fun optionalReceiptData(limits: OptionalReceiptLimits): String =
            (if (limits.active) "1" else "0") +
                limits.lowerRials.coerceIn(0, OptionalReceiptLimits.MAX_AMOUNT_RIALS).toString().padStart(12, '0') +
                limits.upperRials.coerceIn(0, OptionalReceiptLimits.MAX_AMOUNT_RIALS).toString().padStart(12, '0')
    }
}
/*
This transaction is used for TMS update, logon and change keys.
Request to Switch:
Bit Data Element Name Attribute Request Comments
Message Type Id n 4 0800
Bit Map b 8 M Mandatory
03 Processing Code n 6 920000
11 Systems Trace No n 6 M
22 POS Entry Mode n 3 021 *
24 NII n 3 007
25 POS Condition Code n 2 14 *
41 Card acceptor Terminal Id ans 8 M
42 Card acceptor Identification code ans 15 M Merchant code
59 Transport data ans ....999 M *
64 Message Auth. Code b 8 M MAC

*/
fun String.toEnglishNumber(): String {
    return this
        .replace('۰', '0')
        .replace('۱', '1')
        .replace('۲', '2')
        .replace('۳', '3')
        .replace('۴', '4')
        .replace('۵', '5')
        .replace('۶', '6')
        .replace('۷', '7')
        .replace('۸', '8')
        .replace('۹', '9')
        .replace('٠', '0')
        .replace('١', '1')
        .replace('٢', '2')
        .replace('٣', '3')
        .replace('٤', '4')
        .replace('٥', '5')
        .replace('٦', '6')
        .replace('٧', '7')
        .replace('٨', '8')
        .replace('٩', '9')
}