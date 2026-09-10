package com.danesh.hp.bill

import com.danesh.api.BillUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
import com.danesh.hp.key.HpKeyConfig.ASYCUDA
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bill Payment — Save (Function Code 508)، مالی.
 */
@Singleton
class HpBillPaymentMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BillUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.BILL_PAYMENT
        val billNumber = request.billId.trim().take(7)
        val serviceId = request.payId.trim().take(8)
        val serviceNumber = serviceId.padStart(8, '0').takeLast(8)
        val billAmount = request.amount.filter { it.isDigit() }.ifBlank { "0" }
        return messageProvider.create().apply {
            mti = profile.mti
            processingCode = profile.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = billAmount
            dateTime = session.dateTime
            currency = session.currency
            tt51 = HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
            track2 = messageSupport.normalizeTrack2(request.track2)
            messageSupport.run { applyHpAcceptorIds() }
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = profile.emptyMac
            setField48 {
                setTransactionType("508")
                setTerminalType("2")
                setFinancialTransactionIndicator("0")
                setField48Tag("033", ASYCUDA)
                setField48Tag("044", serviceNumber)
                setField48Tag("850", billNumber)
                setField48Tag("856", serviceId)
                setField48Tag("857", billAmount.padStart(12, '0').takeLast(12))
                if (request.requestId.isNotBlank()) {
                    setField48Tag("898", request.requestId.trim().take(12))
                }
            }
        }
    }
}
