package com.danesh.sadad.inquirybnpl

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/** 14-INQUIRY BNPL (MTI 0100/0110، DE3 690000). */
data class SadadInquiryBnplRequest(val clubId: String)

/**
 * DE63 نمونه‌ی سند: 0104800201 → Count=01 FunctionCode=048 #len=002 Data=ClubId("01").
 */
@Singleton
class SadadInquiryBnplMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadInquiryBnplRequest): IsoMessage {
        val clock = contextProvider.currentClock()
        val clubId = request.clubId
        val field63 = "01" + "048" + clubId.length.toString().padStart(3, '0') + clubId
        return messageProvider.create().apply {
            mti = SadadKeyConfig.INQUIRY_BNPL_MTI
            processingCode = SadadKeyConfig.INQUIRY_BNPL_PROCESSING_CODE
            stan = messageSupport.nextStan()
            getIsoMessage().set(12, clock.time)
            getIsoMessage().set(13, clock.date.drop(4))
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = field63
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
