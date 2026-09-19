package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 9-REVERSAL: برگرداندن یک تراکنش قبلی که پاسخش نامشخص مانده — MTI 0400/0410.
 * طبق سند: Processing Code، Amount و STAN دقیقاً «همان پیام اصلی» باید باشند؛
 * DE24 (NII) برخلاف پیاده‌سازی قبلی یک مقدار ثابت (007) است، نه Function Code.
 */
@Singleton
class SadadReverseMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        val amount = item.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val stan = item.stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val processingCode = item.processingCode.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val originalRrn = item.rrn?.trim().orEmpty().takeIf { it.isNotBlank() }
        return messageProvider.create().apply {
            mti = SadadKeyConfig.REVERSE_MTI
            this.processingCode = processingCode
            this.amount = amount
            this.stan = stan
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = item.terminalId
            merchantId = item.merchantId
            originalRrn?.let { setRrn(normalizeRrn(it)) }
            messageSupport.run { setSadadTransportData(transportData()) }
            privateUseField63 = messageSupport.functionCode040Field63()
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private fun normalizeRrn(rrn: String): String {
        val digits = rrn.filter { it.isDigit() }
        return if (digits.length >= 12) digits.takeLast(12) else rrn.take(12).padStart(12, '0')
    }
}
