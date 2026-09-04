package com.danesh.sadad.topup

import com.danesh.api.TopUpUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadTopUpMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: TopUpUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val amount = request.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TOPUP_MTI
            processingCode = SadadKeyConfig.TOPUP_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            this.amount = amount
            dateTime = session.dateTime
            messageSupport.run { applySadadStandardTerminalFields() }
            messageSupport.run { applySadadFunctionCode(SadadKeyConfig.TOPUP_FUNCTION_CODE) }
            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.PURCHASE.emptyMac
            setField48 {
                setTransactionType(SadadKeyConfig.TOPUP_FUNCTION_CODE)
                setTerminalType("2")
                setField48Tag(SadadKeyConfig.OPERATOR_TAG, request.operatorCode)
                setField48Tag(SadadKeyConfig.MOBILE_TAG, request.mobileNumber)
                setField48Tag(SadadKeyConfig.VOUCHER_AMOUNT_TAG, amount)
            }
        }
    }
}
