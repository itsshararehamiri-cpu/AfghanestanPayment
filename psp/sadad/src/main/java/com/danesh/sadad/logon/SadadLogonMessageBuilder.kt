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
            mti = TransactionIsoProfile.LOGON.mti
            processingCode = TransactionIsoProfile.LOGON.processingCode
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
            terminalId = config.terminalId
            merchantId = config.merchantId
            pointOfServiceEntryMode = config.pointOfServiceEntryMode
            currency = config.currency
            nii = TransactionIsoProfile.LOGON.messageNii ?: config.nii
            mac = TransactionIsoProfile.LOGON.emptyMac
        }
    }
}
