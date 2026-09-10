package com.danesh.hp.cash_deposit

import com.danesh.api.CashDepositUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpCashDepositMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CashDepositUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.CASH_DEPOSIT.mti
            processingCode = TransactionIsoProfile.CASH_DEPOSIT.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = request.amount
            dateTime = session.dateTime
            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.CASH_DEPOSIT.emptyMac
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType("618")
            }
        }
    }
}
