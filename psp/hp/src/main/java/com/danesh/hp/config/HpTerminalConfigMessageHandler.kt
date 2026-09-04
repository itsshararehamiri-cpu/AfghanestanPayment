package com.danesh.hp.config



import android.util.Log
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
class HpTerminalConfigMessageHandler @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.TERMINAL_CONFIG.mti
            //processingCode = TransactionIsoProfile.SIGNON.processingCode
            // TODO:
            transmissionDateTime="${clock.date.drop(4)}${clock.time}"
            Log.d("TAG", "build: vvvkkkkkkghghgh${clock.date.drop(2)}${clock.time}")
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
//            terminalId = config.terminalId
//            merchantId = config.merchantId
//            pointOfServiceEntryMode = config.pointOfServiceEntryMode
//            currency = config.currency
            nii = TransactionIsoProfile.SIGNON.messageNii ?: config.nii
            //  mac = TransactionIsoProfile.INIT.emptyMac
        }
    }
}
