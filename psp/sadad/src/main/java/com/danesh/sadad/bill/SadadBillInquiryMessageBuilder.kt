package com.danesh.sadad.bill

import com.danesh.api.BillInquiryRequest
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadBillInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BillInquiryRequest): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BILL_INQUIRY_MTI
            processingCode = SadadKeyConfig.BILL_INQUIRY_PROCESSING_CODE
           // amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode= SadadKeyConfig.POS_CONDITION_CODE

            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(
                48,
                messageSupport.billPaymentField48(request.billId, request.payId),
            )
            //pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }

            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    companion object {
        private const val FUNCTION_CODE = "511"
        private const val TERMINAL_TYPE_POS = "2"
        private const val NON_FINANCIAL = "0"
        private const val ZERO_AMOUNT = "000000000000"
    }
}
