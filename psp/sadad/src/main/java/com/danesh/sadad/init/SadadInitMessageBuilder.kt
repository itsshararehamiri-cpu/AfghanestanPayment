package com.danesh.sadad.init

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadInitMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.INIT_MTI
            processingCode = SadadKeyConfig.INIT__PROCESSING_CODE
            stan = contextProvider.nextStan()
           // dateTime = "${clock.date.drop(2)}${clock.time}"
            terminalId = config.terminalId
            merchantId = config.merchantId
            pointOfServiceEntryMode = config.pointOfServiceEntryMode
            posConditionCode= SadadKeyConfig.POS_CONDITION_CODE

            nii = SadadKeyConfig.SADAD_NII
            mac = TransactionIsoProfile.INIT.emptyMac
        }
    }
}
