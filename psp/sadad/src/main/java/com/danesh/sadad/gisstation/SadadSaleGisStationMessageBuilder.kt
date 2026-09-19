package com.danesh.sadad.gisstation

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 24-SALE GIS STATION: MTI 0200 (پاسخ 0210) / DE3 740000.
 * سند اشاره می‌کند «GIS Station Function Code: 60» بدون مشخص‌کردن Data دقیق آن؛
 * فعلاً فقط ورودی عمومی Function Code 040 در DE63 فرستاده می‌شود.
 */
@Singleton
class SadadSaleGisStationMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadSaleGisStationRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.SALE_GIS_STATION_MTI
            processingCode = SadadKeyConfig.SALE_GIS_STATION_PROCESSING_CODE
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
            privateUseField61 = messageSupport.multiMerchantModeOne()
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
