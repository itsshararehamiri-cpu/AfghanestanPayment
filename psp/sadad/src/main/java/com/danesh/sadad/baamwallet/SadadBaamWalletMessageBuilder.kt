package com.danesh.sadad.baamwallet

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/** 23-BAAM WALLET INQUIRY: MTI 0100 (پاسخ 0110) / DE3 240000. */
@Singleton
class SadadBaamWalletMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadBaamWalletRequest): IsoMessage {
        val field63 = "01" + SadadKeyConfig.BAAM_WALLET_FUNCTION_CODE +
            request.identifier.length.toString().padStart(3, '0') + request.identifier
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BAAM_WALLET_MTI
            processingCode = SadadKeyConfig.BAAM_WALLET_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = field63
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
