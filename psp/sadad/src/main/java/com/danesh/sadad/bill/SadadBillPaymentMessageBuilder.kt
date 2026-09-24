package com.danesh.sadad.bill

import com.danesh.api.BillUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.iso.requireSadad
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پرداخت قبض سداد — درخواست به سوئیچ (Bill_Payment).
 *
 * MTI 0200 (پاسخ 0210)
 * DE3  170000
 * DE4  مبلغ ۱۲ رقمی (مبلغ واردشده، وگرنه استخراج از شناسه پرداخت)
 * DE11 STAN
 * DE22 021
 * DE24 007 NII
 * DE25 14
 * DE35 Track 2
 * DE41 Terminal Id
 * DE42 Acceptor Id
 * DE48 Bill_ID(13) + Payment_ID(13)
 * DE52 PIN
 * DE59 Transport data
 * DE64 MAC
 */
@Singleton
class SadadBillPaymentMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
) {
    suspend fun build(request: BillUserInput): IsoMessage {
        messageSupport.beginSession()
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.BILL_MTI
            processingCode = SadadKeyConfig.BILL_PROCESSING_CODE
            amount = SadadBillFields.resolvePaymentAmount(request.amount, request.payId)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            transportData = messageSupport.initTransportData()
        }
        message.requireSadad().setPlainField48(
            SadadBillFields.field48(request.billId, request.payId),
        )
        //message.requireSadad().unsetFields(60, 61, 63)
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)


        return message
    }
}
