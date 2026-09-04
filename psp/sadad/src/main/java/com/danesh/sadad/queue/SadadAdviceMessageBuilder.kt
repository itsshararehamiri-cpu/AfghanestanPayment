package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.api.TransactionClock
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadAdviceMessageBuilder @Inject constructor(
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        sessionClock.capture(TransactionClock(date = item.date, time = item.time))
        return messageProvider.create().apply {
            mti = "1420"
            processingCode = item.processingCode
            amount = item.amount
            stan = item.stan
            dateTime = "${item.date.drop(2)}${item.time}"
            terminalId = item.terminalId
            merchantId = item.merchantId
            currency = item.currency
            item.rrn?.let { setRrn(it) }
        }
    }
}
