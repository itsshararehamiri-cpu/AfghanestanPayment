package com.danesh.sadad.bill

import com.danesh.api.BillInquiryRequest
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * استعلام قبض سداد (6-BILL INQUIRY سند) — درخواست به سوئیچ.
 *
 * MTI 0100 / DE3 170000 / DE22 021 / DE24 007 (NII) / DE25 14 / DE35 Track2
 * DE48 = Bill_ID(13) + Payment_ID(13) — همان قالب خام پرداخت قبض، بدون PIN (سند PIN را برای
 * این تراکنش ذکر نکرده است).
 */
@Singleton
class SadadBillInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BillInquiryRequest): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BILL_INQUIRY_MTI
            processingCode = SadadKeyConfig.BILL_PROCESSING_CODE
            amount = ZERO_AMOUNT
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.BILL_POS_ENTRY_MODE
            nii = SadadKeyConfig.BILL_NII
            messageReasonCode = SadadKeyConfig.BILL_POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            dateTime = session.dateTime
            currency = session.currency
            getIsoMessage().set(
                48,
                messageSupport.billPaymentField48(request.billId, request.payId),
            )
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private companion object {
        const val ZERO_AMOUNT = "000000000000"
    }
}
