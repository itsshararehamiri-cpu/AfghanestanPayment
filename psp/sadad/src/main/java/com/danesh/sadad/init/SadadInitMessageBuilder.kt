package com.danesh.sadad.init

import android.util.Log
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadInitMessageBuilder @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val sessionClock: TransactionSessionClock,
    private val messageProvider: IsoMessageProvider,
    private val messageSupport: SadadIsoMessageSupport,private val macCalculator: SadadMacCalculator,
) {
   suspend fun build(): IsoMessage {
       Log.d("TAG", "build: SadadInitMessageBuilder")
        val clock = contextProvider.currentClock()
        sessionClock.capture(clock)
        // مستند: DE11 پیام INIT باید یک عدد تصادفی ۶ رقمی باشد، نه شمارنده‌ی ترتیبی nextStan().
      //  val stan = (0..999999).random().toString().padStart(6, '0')
        // شماره ترمینال INIT: ۶ رقم ساعت/دقیقه/ثانیه + ۲ رقم آخر trace number (تصادفی، صرفاً برای فعال‌سازی)
       val tempStan=contextProvider.nextStan()
        val randomTerminalId = (clock.time + tempStan.takeLast(2)).takeLast(SadadKeyConfig.TERMINAL_ID_LENGTH)
        val placeholderMerchantId = "1".repeat(SadadKeyConfig.MERCHANT_ID_LENGTH)
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.INIT_MTI
            processingCode = SadadKeyConfig.INIT__PROCESSING_CODE
            this.stan = tempStan
            terminalId = randomTerminalId
            merchantId = placeholderMerchantId
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            nii = SadadKeyConfig.SADAD_NII
            transportData = messageSupport.initTransportData()
        }
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyInitMac(message)
        return message

    }
}
