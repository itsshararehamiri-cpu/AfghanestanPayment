package com.danesh.hp.signon

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network sign-on همراه‌پی (MTI 1804) — طبق سند پروتکل کارن فقط DE0, DE7, DE11, DE12, DE24
 * مجاز است؛ DE41/DE42/DE22/DE49 (شماره پایانه/پذیرنده، POS entry mode، ارز) در پیام sign-on
 * ارسال نمی‌شوند و فقط در تراکنش‌های مالی/پیکربندی پایانه به‌کار می‌روند.
 */
@Singleton
class HpSignOnMessageHandler @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti = TransactionIsoProfile.SIGNON.mti
            transmissionDateTime = "${clock.date.drop(4)}${clock.time}"
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
            nii = TransactionIsoProfile.SIGNON.messageNii ?: config.nii
        }
    }
}
