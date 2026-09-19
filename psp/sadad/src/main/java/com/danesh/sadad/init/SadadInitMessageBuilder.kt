package com.danesh.sadad.init

import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadInitMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
    private val messageSupport: SadadIsoMessageSupport,
) {
    fun build(): IsoMessage {
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        // مستند: DE11 پیام INIT باید یک عدد تصادفی ۶ رقمی باشد، نه شمارنده‌ی ترتیبی nextStan().
        val stan = (0..999999).random().toString().padStart(6, '0')
        // شماره ترمینال INIT: ۶ رقم ساعت/دقیقه/ثانیه + ۲ رقم آخر trace number (تصادفی، صرفاً برای فعال‌سازی)
        val randomTerminalId = (clock.time + stan.takeLast(2)).takeLast(SadadKeyConfig.TERMINAL_ID_LENGTH)
        val placeholderMerchantId = "1".repeat(SadadKeyConfig.MERCHANT_ID_LENGTH)
        return messageProvider.create().apply {
            mti = SadadKeyConfig.INIT_MTI
            processingCode = SadadKeyConfig.INIT__PROCESSING_CODE
            this.stan = stan
            terminalId = randomTerminalId
            merchantId = placeholderMerchantId
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            posConditionCode= SadadKeyConfig.POS_CONDITION_CODE

            nii = SadadKeyConfig.SADAD_NII
            transportData = ""
            messageSupport.run { setSadadTransportData(initTransportData()) }
            mac = TransactionIsoProfile.INIT.emptyMac
        }
    }
}
