package com.danesh.sadad.inquirystatus

import com.danesh.api.TransactionContextProvider
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 13-INQUIRY STATUS: MTI 0100 (پاسخ 0110) / DE3 330000.
 * برخلاف بقیه‌ی تراکنش‌ها، DE12/DE13 (زمان/تاریخ محلی) و DE61 (ماه/روز موردنظر)
 * در همین درخواست پر می‌شوند، نه فقط در پاسخ.
 */
@Singleton
class SadadInquiryStatusMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: SadadInquiryStatusRequest): IsoMessage {
        val clock = contextProvider.currentClock()
        val month = request.month.filter { it.isDigit() }.padStart(2, '0').takeLast(2)
        val day = request.day.filter { it.isDigit() }.padStart(2, '0').takeLast(2)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.INQUIRY_STATUS_MTI
            processingCode = SadadKeyConfig.INQUIRY_STATUS_PROCESSING_CODE
            stan = messageSupport.nextStan()
            getIsoMessage().set(12, clock.time)
            getIsoMessage().set(13, clock.date.drop(4))
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId = messageSupport.merchantIdOrDefault()
            getIsoMessage().set(48, SadadKeyConfig.INQUIRY_STATUS_FIELD48)
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField61 = month + day
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
