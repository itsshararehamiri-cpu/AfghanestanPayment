package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.iso.packager.SadadIso93BPackager
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.mac.SadadMacCalculator
import com.danesh.sadad.util.Field63Generator
import com.danesh.sadad.util.FunctionCodeData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 9-REVERSAL سداد — برگشت تراکنشی که پاسخش نرسیده. پاسخ 0410.
 *
 * MTI 0400
 * DE3  همان Processing Code اصلی
 * DE4  همان مبلغ
 * DE11 همان STAN
 * DE22 021
 * DE24 007
 * DE25 14
 * DE35 همان Track 2
 * DE41 ترمینال
 * DE42 پذیرنده
 * DE59 Transport data
 * DE63 Private4 — اگر تراکنش اصلی داشته باشد همان، وگرنه Function Code 040
 * DE64 MAC
 */
@Singleton
class SadadReverseMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
    private val macCalculator: SadadMacCalculator,
) {
    suspend fun build(item: QueueItem): IsoMessage {
        val message = messageProvider.create().apply {
            mti = SadadKeyConfig.REVERSE_MTI
            processingCode = digits(item.processingCode, 6)
            amount = digits(item.amount, 12)
            stan = digits(item.stan, 6)
            pointOfServiceEntryMode = SadadKeyConfig.POS_ENTRY_MODE
            nii = SadadKeyConfig.SADAD_NII
            posConditionCode = SadadKeyConfig.POS_CONDITION_CODE
            item.track2?.takeIf { it.isNotBlank() }?.let { track2 = it }
            terminalId = pad(item.terminalId, SadadKeyConfig.TERMINAL_ID_LENGTH)
            merchantId = pad(item.merchantId, SadadKeyConfig.MERCHANT_ID_LENGTH)
            transportData = messageSupport.initTransportData()
            privateUseField63 = field63(item)
        }
        message.setPackager(SadadIso93BPackager())
        macCalculator.applyTransactionMac(message)
        return message
    }
}

internal fun digits(raw: String, length: Int): String =
    raw.filter { it.isDigit() }.padStart(length, '0').takeLast(length)

internal fun pad(raw: String, length: Int): String =
    raw.padEnd(length).take(length)

internal fun rrn(raw: String?): String {
    val trimmed = raw?.trim().orEmpty()
    return if (trimmed.length >= 12) trimmed.takeLast(12) else trimmed.padStart(12, '0')
}

/** Check attention: دادهٔ فیلد ۶۳ اصلی، و اگر نباشد فقط Function Code 040. */
internal fun field63(item: QueueItem): String =
    item.originalField63?.takeIf { it.isNotBlank() }
        ?: Field63Generator.generate(
            listOf(FunctionCodeData(SadadKeyConfig.FUNCTION_CODE_CONNECTION, "")),
        )
