package com.danesh.sadad.init

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * فعال‌سازی/مقداردهی ترمینال سداد (11-TERMINAL INITIALIZER سند) — درخواست به سوئیچ.
 *
 * MTI 0800 / DE3 930000 / DE22 021 / DE24 007 (NII) / DE25 14. پاسخ سوئیچ کلیدهای کاری
 * موبایل/Mpos را در DE48 (رمزشده با کلید مستر مربوطه) برمی‌گرداند — طبق سند، تفسیر و اعمال آن
 * روی HSM/کیبورد فیزیکی نیازمند یکپارچه‌سازی جداگانه‌ای است که خارج از این تغییر است.
 */
@Singleton
class SadadInitMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(): IsoMessage {
        val config = contextProvider.getTerminalConfig()
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.TERMINAL_INITIALIZER_MTI
            processingCode = SadadKeyConfig.TERMINAL_INITIALIZER_PROCESSING_CODE
            stan = contextProvider.nextStan()
            dateTime = "${clock.date.drop(2)}${clock.time}"
            terminalId = config.terminalId.ifBlank { SadadKeyConfig.DEFAULT_TERMINAL_ID }
            merchantId = config.merchantId.ifBlank { SadadKeyConfig.DEFAULT_MERCHANT_ID }
            pointOfServiceEntryMode = SadadKeyConfig.ISO_POS_ENTRY_MODE
            messageReasonCode = SadadKeyConfig.POS_CONDITION_CODE
            currency = config.currency
            nii = SadadKeyConfig.NII
            mac = SadadKeyConfig.EMPTY_MAC
        }
    }
}
