package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * برگشت تراکنش سداد (8-REVERSAL سند) — درخواست به سوئیچ.
 *
 * MTI 0400 / DE3، DE4، DE11، DE35 = دقیقاً برابر پیام اصلی («Same as Original Message»)،
 * DE22 021 / DE24 007 (NII) / DE25 14. سند برای این تراکنش فیلد 48 تعریف نکرده است.
 */
@Singleton
class SadadReverseMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        val session = messageSupport.beginSession()
        val pan = item.sourcePan?.filter { it.isDigit() }.orEmpty()
        val amount = item.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val stan = item.stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val originalRrn = item.rrn?.trim().orEmpty().takeIf { it.isNotBlank() }
        return messageProvider.create().apply {
            mti = SadadKeyConfig.REVERSE_MTI
            this.pan = pan
            processingCode = item.processingCode.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
            this.amount = amount
            this.stan = stan
            pointOfServiceEntryMode = SadadKeyConfig.ISO_POS_ENTRY_MODE
            nii = SadadKeyConfig.NII
            messageReasonCode = SadadKeyConfig.POS_CONDITION_CODE
            terminalId = item.terminalId
            merchantId = item.merchantId
            originalRrn?.let { setRrn(normalizeRrn(it)) }
            dateTime = session.dateTime
            messageSupport.run { setSadadTransportData(transportData()) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }

    private fun normalizeRrn(rrn: String): String {
        val digits = rrn.filter { it.isDigit() }
        return if (digits.length >= 12) digits.takeLast(12) else rrn.take(12).padStart(12, '0')
    }
}
