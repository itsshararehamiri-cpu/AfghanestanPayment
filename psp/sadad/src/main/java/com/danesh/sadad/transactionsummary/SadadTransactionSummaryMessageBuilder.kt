package com.danesh.sadad.transactionsummary

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/** 18-TRANSACTION SUMMARY: MTI 0100 (پاسخ 0110) / DE3 430000. */
@Singleton
class SadadTransactionSummaryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadTransactionSummaryRequest): IsoMessage {
        messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TRANSACTION_SUMMARY_MTI
            processingCode = SadadKeyConfig.TRANSACTION_SUMMARY_PROCESSING_CODE
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
