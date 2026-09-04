package com.danesh.sadad.balance

import com.danesh.api.BalanceUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * موجودی سداد — درخواست به سوئیچ طبق سند 2-BALANCE.
 *
 * MTI 0100
 * DE3  310000  Processing Code
 * DE11 STAN
 * DE22 021     POS Entry Mode (n 3)
 * DE24 007     NII
 * DE25 14      POS Condition Code (n 2)
 * DE35 Track 2
 * DE41 Terminal Id (ans 8)
 * DE42 Acceptor Id (ans 15)
 * DE52 PIN block
 * DE59 Transport data
 * DE64 MAC (8 بایت خالی)
 */
@Singleton
class SadadBalanceMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BalanceUserInput): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BALANCE_MTI
            processingCode = SadadKeyConfig.BALANCE_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.BALANCE_POS_ENTRY_MODE
            nii = SadadKeyConfig.BALANCE_NII
            messageReasonCode = SadadKeyConfig.BALANCE_POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
