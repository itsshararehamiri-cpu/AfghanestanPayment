package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 8-ADVICE سداد — تأیید تراکنش موفق. پاسخ 0230.
 *
 * MTI 0220
 * DE3  همان Processing Code اصلی
 * DE11 همان STAN
 * DE22 021
 * DE24 007
 * DE25 14
 * DE37 RRN فروش
 * DE41 ترمینال
 * DE42 پذیرنده
 * DE59 Transport data
 * DE63 Private4 — اگر تراکنش اصلی داشته باشد همان، وگرنه Function Code 040
 * DE64 MAC
 */
@Singleton
class SadadAdviceMessageBuilder @Inject constructor(
    private val messageProvider: IsoMessageProvider,
    private val messageSupport: SadadIsoMessageSupport,
    private val macCalculator: SadadMacCalculator,
) {
    suspend fun build(item: QueueItem): IsoMessage {
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.ADVICE_MTI
            processingCode = digits(item.processingCode, 6)
            stan = digits(item.stan, 6)
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            setRrn(rrn(item.rrn))
//            terminalId = pad(item.terminalId, SadadKeyConfig.TERMINAL_ID_LENGTH)
//            merchantId = pad(item.merchantId, SadadKeyConfig.MERCHANT_ID_LENGTH)
            terminalId = messageSupport.terminalIdOrDefault()
            merchantId =messageSupport.merchantIdOrDefault()
            transportData = messageSupport.initTransportData()
            privateUseField63 = field63(item)
        }
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }
}
