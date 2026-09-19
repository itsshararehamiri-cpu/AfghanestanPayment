package com.danesh.sadad.kahroba

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadKahrobaMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun buildSale(request: SadadKahrobaSaleRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.PURCHASE_MTI
            processingCode = SadadKeyConfig.PURCHASE_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.KAHROBA_POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, messageSupport.additionalPrivateData())
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            getIsoMessage().set(55, ISOUtil.hex2byte(request.iccData))
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField61 = messageSupport.multiMerchantModeOne()
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    fun buildBalance(request: SadadKahrobaBalanceRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BALANCE_MTI
            processingCode = SadadKeyConfig.BALANCE_PROCESSING_CODE
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.KAHROBA_POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            getIsoMessage().set(55, ISOUtil.hex2byte(request.iccData))
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
