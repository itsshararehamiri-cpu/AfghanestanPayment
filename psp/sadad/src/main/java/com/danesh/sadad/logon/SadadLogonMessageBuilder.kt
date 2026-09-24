package com.danesh.sadad.logon

import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
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
        macCalculator.applyLogonMac(message)
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
fun buildField59(
    structureVersion: Int = 3,
    connectionAttempts: Int,
    lastTimeDone: Int,
    hardwareVersion: String,
    softwareVersion: String,
    firmwareVersion: String,
    serialNumber: String,
    masterKeyIndex: Int,
    reserve: Int = 0,
    encryptionMethod: Int = 4
): String {

    require(structureVersion == 3) {
        "Structure Version must be 3"
    }

    require(connectionAttempts in 0..99) {
        "Connection Attempts must be 2 digits"
    }

    require(lastTimeDone in 0..99) {
        "Last Time Done must be 2 digits"
    }

    require(hardwareVersion.length == 5) {
        "HW must be exactly 5 characters"
    }

    require(softwareVersion.length == 6) {
        "SW must be exactly 6 characters"
    }

    require(firmwareVersion.length == 6) {
        "FW must be exactly 6 characters"
    }

    require(serialNumber.length <= 99) {
        "Serial Number cannot exceed 99 characters"
    }

    require(masterKeyIndex in 0..999) {
        "Master Key Index must be 3 digits"
    }

    require(encryptionMethod == 4) {
        "Encryption Method must be 4"
    }

    val temp =   buildString {

        // Structure Version - n1
        append(structureVersion)

        // Connection Attempts - n2
        append(String.format(Locale.US, "%02d", connectionAttempts))

        // Last Time Done - n2
        append(String.format(Locale.US, "%02d", lastTimeDone))

        // POS Information - HW ans5
        append(hardwareVersion.toEnglishNumber())

        // SW ans6
        append(softwareVersion.toEnglishNumber())

        // FW ans6
        append(firmwareVersion.toEnglishNumber())

        // S.NO Length - n2
        append(String.format(Locale.US, "%02d", "D1V2890000001".length))//

        // S.NO - LLVAR
        append("D1V2890000001".toEnglishNumber())

        // Master Key Index - n3
        append(String.format(Locale.US, "%03d", masterKeyIndex))

        // Reserve - n3 = 000
        append("000")

        // Encryption Method - n1
        append(encryptionMethod)
    }

    Log.d("SADAD", "DE59 = '$temp'")

    temp.forEachIndexed { index, char ->
        Log.d(
            "SADAD",
            "DE59[$index]='$char' code=${char.code}"
        )
    }

    return temp
}
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