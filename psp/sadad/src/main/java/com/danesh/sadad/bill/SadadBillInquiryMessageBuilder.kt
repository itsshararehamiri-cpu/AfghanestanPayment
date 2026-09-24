package com.danesh.sadad.bill

import com.danesh.api.BillInquiryRequest
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.iso.requireSadad
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * استعلام قبض سداد — درخواست به سوئیچ (6-BILL INQUIRY).
 *
 * MTI 0100 (پاسخ 0110)
 * DE3  170000
 * DE4  مبلغ ۱۲ رقمی (مبلغ واردشده در صفحه قبض)
 * DE11 STAN
 * DE22 021
 * DE24 007 NII
 * DE25 14
 * DE35 Track 2
 * DE41 Terminal Id
 * DE42 Acceptor Id
 * DE48 Bill_ID(13) + Payment_ID(13)
 * DE59 Transport data
 * DE63 Function Code 008
 * DE64 MAC
 */
@Singleton
class SadadBillInquiryMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
) {
    suspend fun build(request: BillInquiryRequest): IsoMessage {
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.BILL_INQUIRY_MTI
            processingCode = SadadKeyConfig.BILL_INQUIRY_PROCESSING_CODE
            amount = inquiryAmount(request)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            if (request.track2.isNotBlank()) {
                track2 = messageSupport.normalizeTrack2(request.track2)
            }
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            transportData = messageSupport.initTransportData()
            //privateUseField63 = SadadBillFields.inquiryField63()
        }
        message.requireSadad().setPlainField48(
            SadadBillFields.field48(request.billId, request.payId),
        )
   //     message.requireSadad().unsetFields(60, 61)
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun inquiryAmount(request: BillInquiryRequest): String {
        val entered = request.amount.filter(Char::isDigit)
        if (entered.isNotBlank() && entered.any { it != '0' }) {
            return messageSupport.formatIsoAmount(request.amount)
        }
        return SadadBillFields.amountFromPaymentId(request.payId)
    }
}
