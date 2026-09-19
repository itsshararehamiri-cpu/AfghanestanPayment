package com.danesh.sadad.toll

import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadTollMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun buildInquiry(request: SadadTollInquiryRequest): IsoMessage {
        val orgId = request.organizationId.filter { it.isDigit() }.padStart(3, '0').takeLast(3)
        val orgData = request.organizationData
        val orgDataLen = orgData.length.toString().padStart(3, '0')
        val dataLength = (orgId.length + orgDataLen.length + orgData.length).toString().padStart(3, '0')
        val field63 = "003" + dataLength + orgId + orgDataLen + orgData
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TOLL_INQUIRY_MTI
            processingCode = SadadKeyConfig.TOLL_INQUIRY_PROCESSING_CODE
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

    fun buildPayment(request: SadadTollPaymentRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TOLL_PAYMENT_MTI
            processingCode = SadadKeyConfig.TOLL_PAYMENT_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, request.additionalData)
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            if (request.private2Data.isNotBlank()) {
                privateUseField61 = request.private2Data
            }
            if (request.private4Data.isNotBlank()) {
                privateUseField63 = request.private4Data
            }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
