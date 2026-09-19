package com.danesh.sadad.gambond

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/** 22-GAM BOND: MTI 0200 (پاسخ 0210) / DE3 700000. */
@Singleton
class SadadGamBondMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadGamBondRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.GAM_BOND_MTI
            processingCode = SadadKeyConfig.GAM_BOND_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, messageSupport.additionalPrivateData())
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
