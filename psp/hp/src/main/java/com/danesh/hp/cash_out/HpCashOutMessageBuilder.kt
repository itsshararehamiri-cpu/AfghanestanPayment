package com.danesh.hp.cash_out

import com.danesh.api.CashOutUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpCashOutMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CashOutUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.CASH_OUT.mti
            processingCode = TransactionIsoProfile.CASH_OUT.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = request.amount
            dateTime = session.dateTime
           // messageSupport.run { applyHpStandardTerminalFields() }
//            messageSupport.run { applyHpFunctionCode(TransactionIsoProfile.CASH_OUT) }

            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.CASH_OUT.emptyMac
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType("700")
                setTerminalType("2")
                setCard2NNumber(request.destinationAccount)
                setFinancialTransactionIndicator("1")
            }
        }
    }
}
