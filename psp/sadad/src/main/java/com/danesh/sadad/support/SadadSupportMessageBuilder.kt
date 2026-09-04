package com.danesh.sadad.support

import com.danesh.api.SupportUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadSupportMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SupportUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val amount = request.amount.filter { it.isDigit() }.ifBlank { "0" }
        return messageProvider.create().apply {
            mti = SadadKeyConfig.SUPPORT_MTI
            processingCode = SadadKeyConfig.SUPPORT_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            this.amount = amount
            dateTime = session.dateTime
            messageSupport.run { applySadadStandardTerminalFields() }
            messageSupport.run { applySadadFunctionCode(SadadKeyConfig.SUPPORT_FUNCTION_CODE) }
            currency = session.currency
            track2 = messageSupport.normalizeTrack2(request.track2)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = TransactionIsoProfile.SUPPORT.emptyMac
            setField48 {
                setTransactionType(SadadKeyConfig.SUPPORT_FUNCTION_CODE)
                setTerminalType("2")
                setField48Tag(SadadKeyConfig.SUPPORT_SERVICE_TAG, request.serviceId)
            }
        }
    }
}
