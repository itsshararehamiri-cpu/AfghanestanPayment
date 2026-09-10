package com.danesh.hp.balance

import com.danesh.api.BalanceUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpBalanceMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider
) {
    fun build(request: BalanceUserInput): IsoMessage {
        val session = messageSupport.beginSession()

        return messageProvider.create().apply {
            mti = TransactionIsoProfile.BALANCE.mti
            processingCode = TransactionIsoProfile.BALANCE.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = "0"
            dateTime = session.dateTime
            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType("702")
            }
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.BALANCE.emptyMac
        }
    }
}
