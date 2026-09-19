package com.danesh.sadad.acceptpin

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/** 19-ACCEPT PIN: MTI 0100 (پاسخ 0110) / DE3 710000. */
@Singleton
class SadadAcceptPinMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadAcceptPinRequest): IsoMessage {
        messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = SadadKeyConfig.ACCEPT_PIN_MTI
            processingCode = SadadKeyConfig.ACCEPT_PIN_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
