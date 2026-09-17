package com.danesh.sadad.name_inquiry

import com.danesh.api.NameInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadNameInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: NameInquiryRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.NAME_INQUIRY_MTI
            processingCode = SadadKeyConfig.NAME_INQUIRY_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode= SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, messageSupport.additionalPrivateData())
       //     pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField61 = messageSupport.multiMerchantModeOne()
            getIsoMessage().set(63, "")

            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    companion object {
        private const val FUNCTION_CODE = "651"
    }
}
