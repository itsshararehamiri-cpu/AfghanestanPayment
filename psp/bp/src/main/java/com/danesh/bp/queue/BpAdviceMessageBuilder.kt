package com.danesh.bp.queue

import android.util.Log
import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.QueueItem
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionType
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpAdviceMessageBuilder @Inject constructor(
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
    private val contextProvider: TransactionContextProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
) {

    suspend fun build(item: QueueItem): IsoMessage {
        val message = messageProvider.create().apply {

            mti = BpKeyConfig.ADVICE_MTI
            processingCode = item.processingCode
            amount = formatIsoAmount(item.amount)
            stan = contextProvider.nextStan()
            dateTime = resolveDateTime(item)
            terminalId = item.terminalId
            item.rrn?.takeIf { it.isNotBlank() }?.let { setRrn(it) }
            if (item.type == TransactionType.BILL.ordinal) {
                item.adviceAdditionalData
                    ?.takeIf { it.isNotBlank() }
                    ?.let { additionalResponseData = it }
            }
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.ADVICE_FIELD48_TAG,
                    value = lastSuccessValues.stanTagValue(),
                )
            }
        }
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun formatIsoAmount(amount: String): String {
        val digits = amount.filter(Char::isDigit)
        return digits.padStart(12, '0').takeLast(12)
    }

    private fun resolveDateTime(item: QueueItem): String {
        if (item.date.length >= 8 && item.time.length == 6) {
            return "${item.date.drop(2)}${item.time}"
        }
        return currentLocalDateTime()
    }

    private fun currentLocalDateTime(): String {
        return SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
    }
}
