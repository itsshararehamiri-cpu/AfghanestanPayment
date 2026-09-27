package com.danesh.sadad.logon

import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.common.diagnostics.StartupTraceFile
import com.danesh.sadad.diagnostics.traceIso
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
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
    suspend fun build(): IsoMessage {
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
        }
        StartupTraceFile.line("LogonMessage", "apply MAC")
        macCalculator.applyLogonMac(message)
        traceIso("LogonMessage request", message)
        StartupTraceFile.line("LogonMessage", "build done")
        return message

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