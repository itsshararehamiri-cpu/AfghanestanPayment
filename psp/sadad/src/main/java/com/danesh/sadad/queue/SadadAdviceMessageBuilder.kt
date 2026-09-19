package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 8-ADVICE: تأیید یک تراکنش موفقِ قبلی که پاسخش دریافت نشده — MTI 0220/0230.
 * DE37 (RRN تراکنش فروش اصلی) الزامی است.
 */
@Singleton
class SadadAdviceMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        sessionClock.capture(TransactionClock(date = item.date, time = item.time))
        val normalizedProcessingCode = item.processingCode.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val normalizedStan = item.stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.ADVICE_MTI
            processingCode = normalizedProcessingCode
            stan = normalizedStan
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = item.terminalId
            merchantId = item.merchantId
            item.rrn?.let { setRrn(it) }
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
