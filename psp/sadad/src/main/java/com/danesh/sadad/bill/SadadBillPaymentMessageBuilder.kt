package com.danesh.sadad.bill

import com.danesh.api.BillUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پرداخت قبض سداد — درخواست به سوئیچ.
 *
 * MTI 0200
 * DE3  170000  Processing Code
 * DE4  مبلغ ۱۲ رقمی
 * DE11 STAN
 * DE22 021     POS Entry Mode (n 3)
 * DE24 007     NII
 * DE25 14      POS Condition Code (n 2)
 * DE35 Track 2
 * DE41 Terminal Id (ans 8)
 * DE42 Acceptor Id (ans 15)
 * DE48 Bill_ID(13) + Payment_ID(13) — خام ۲۶ رقم، بدون TLV همراه‌پی
 * DE52 PIN block
 * DE59 Transport data
 * DE64 MAC (8 بایت خالی)
 */
@Singleton
class SadadBillPaymentMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: BillUserInput): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.BILL_MTI
            processingCode = SadadKeyConfig.BILL_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.BILL_POS_ENTRY_MODE
            nii = SadadKeyConfig.BILL_NII
            messageReasonCode = SadadKeyConfig.BILL_POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(
                48,
                messageSupport.billPaymentField48(request.billId, request.payId),
            )
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
