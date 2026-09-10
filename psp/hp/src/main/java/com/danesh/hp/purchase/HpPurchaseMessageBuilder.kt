package com.danesh.hp.purchase

import com.danesh.api.PurchaseUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.common.connection.DefaultDepositIdProvider
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpPurchaseMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val defaultDepositIdProvider: DefaultDepositIdProvider,
) {
    fun build(request: PurchaseUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.PURCHASE.mti
            processingCode = TransactionIsoProfile.PURCHASE.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = request.amount
            dateTime = session.dateTime
           // messageSupport.run { applyHpStandardTerminalFields() }
//            messageSupport.run { applyHpFunctionCode(TransactionIsoProfile.PURCHASE) }
            currency = session.currency
            tt51 = HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.PURCHASE.emptyMac
            messageSupport.run { applyHpAcceptorIds() }
            setField48 {
                setTransactionType("774")
            }
//            defaultDepositIdProvider.activeDepositId()?.let { depositId ->
//                setField48 {
//                    setCard2NNumber(depositId)
//                }
//            }
        }
    }
}
