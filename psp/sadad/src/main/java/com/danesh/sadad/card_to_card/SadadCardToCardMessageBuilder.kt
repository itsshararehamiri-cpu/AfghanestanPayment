package com.danesh.sadad.card_to_card

import com.danesh.api.CardToCardUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadCardToCardMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CardToCardUserInput): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.CARD_TO_CARD_MTI
            processingCode = SadadKeyConfig.CARD_TO_CARD_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)

            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode= SadadKeyConfig.POS_CONDITION_CODE

//            messageReasonCode = SadadKeyConfig.BALANCE_POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            transportData=""
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
