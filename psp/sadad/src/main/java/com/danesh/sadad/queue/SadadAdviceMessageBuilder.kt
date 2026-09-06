package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تأییدیه تراکنش موفق سداد (7-ADVICE سند) — درخواست به سوئیچ.
 *
 * MTI 0220 / DE3، DE11، DE37 = دقیقاً برابر پیام اصلی («Same as Original Message»)،
 * DE22 021 / DE24 007 (NII) / DE25 14.
 */
@Singleton
class SadadAdviceMessageBuilder @Inject constructor(
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        sessionClock.capture(TransactionClock(date = item.date, time = item.time))
        return messageProvider.create().apply {
            mti = SadadKeyConfig.ADVICE_MTI
            processingCode = item.processingCode
            amount = item.amount
            stan = item.stan
            pointOfServiceEntryMode = SadadKeyConfig.ISO_POS_ENTRY_MODE
            nii = SadadKeyConfig.NII
            messageReasonCode = SadadKeyConfig.POS_CONDITION_CODE
            dateTime = "${item.date.drop(2)}${item.time}"
            terminalId = item.terminalId
            merchantId = item.merchantId
            currency = item.currency
            item.rrn?.let { setRrn(it) }
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
