package com.danesh.sadad.purchase

import com.danesh.api.PurchaseUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * خرید سداد — درخواست به سوئیچ (sale / multi-merchant).
 *
 * MTI 0200
 * DE3  000000  Processing Code
 * DE4  مبلغ ۱۲ رقمی
 * DE11 STAN
 * DE22 021     POS Entry Mode (n 3)
 * DE24 007     NII
 * DE25 14      POS Condition Code (n 2)
 * DE35 Track 2
 * DE41 Terminal Id (ans 8)
 * DE42 Acceptor Id (ans 15)
 * DE48 Additional Data – Private (اجباری)
 * DE52 PIN block
 * DE59 Transport data
 * DE61 Mode 1: 01 + slot 01 (یک ترمینال / یک پذیرنده)
 * DE64 MAC (8 بایت خالی)
 *
 * DE60 و DE63 در فروش ساده ارسال نمی‌شوند.
 */
@Singleton
class SadadPurchaseMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: PurchaseUserInput): IsoMessage {
        return messageProvider.create().apply {
            mti = SadadKeyConfig.PURCHASE_MTI
            processingCode = SadadKeyConfig.PURCHASE_PROCESSING_CODE
            amount = messageSupport.formatIsoAmount(request.amount)
            stan = messageSupport.nextStan()
            pointOfServiceEntryMode = SadadKeyConfig.PURCHASE_POS_ENTRY_MODE
            nii = SadadKeyConfig.PURCHASE_NII
            messageReasonCode = SadadKeyConfig.PURCHASE_POS_CONDITION_CODE
            track2 = messageSupport.normalizeTrack2(request.track2)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, messageSupport.additionalPrivateData())
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField61 = messageSupport.multiMerchantModeOne()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
