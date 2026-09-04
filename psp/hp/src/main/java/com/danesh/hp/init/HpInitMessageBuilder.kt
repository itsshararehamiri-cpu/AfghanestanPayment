package com.danesh.hp.init

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Init همراه‌پی — شامل فیلدهای ترمینال (22، 24، 41، 42) از تنظیمات.
 * برخلاف به‌پرداخت، فیلدهای 61/62/63 در Init HP ارسال نمی‌شوند.
 */
@Singleton
class HpInitMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.INIT.mti
            processingCode = TransactionIsoProfile.INIT.processingCode
            transmissionDateTime=""
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
            terminalId = config.terminalId
            merchantId = config.merchantId
            nii ="305"// TransactionIsoProfile.INIT.messageNii ?: config.nii
            f72=""
            mac = TransactionIsoProfile.INIT.emptyMac
        }
    }
}
