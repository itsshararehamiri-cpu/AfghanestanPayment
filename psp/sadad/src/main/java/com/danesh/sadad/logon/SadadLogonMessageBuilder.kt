package com.danesh.sadad.logon

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadLogonMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti ="0800" //TransactionIsoProfile.LOGON.mti
            processingCode ="920000"// TransactionIsoProfile.LOGON.processingCode
            stan = contextProvider.nextStan()
           // dateTime = "${clock.date.drop(2)}${clock.time}"
            terminalId = config.terminalId
            merchantId = config.merchantId
            pointOfServiceEntryMode = config.pointOfServiceEntryMode
            posConditionCode="14"
            currency = config.currency
            nii ="007"// TransactionIsoProfile.LOGON.messageNii ?: config.nii
            transportData=""// TODO:
            mac = TransactionIsoProfile.LOGON.emptyMac
        }
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