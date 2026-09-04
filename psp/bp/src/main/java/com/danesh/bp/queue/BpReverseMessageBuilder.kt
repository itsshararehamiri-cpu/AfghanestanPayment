package com.danesh.bp.queue

import com.danesh.api.PspDeviceMetadataProvider
import com.danesh.api.QueueItem
import com.danesh.api.TransactionType
import com.danesh.bp.field48.BpCancelReasons
import com.danesh.bp.field48.BpField48LastSuccessValues
import com.danesh.bp.mac.BpMacCalculator
import com.danesh.bp.field63.toBpField63
import com.danesh.bp.key.BpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpReverseMessageBuilder @Inject constructor(
    private val metadataProvider: PspDeviceMetadataProvider,
    private val macCalculator: BpMacCalculator,
    private val messageProvider: IsoMessageProvider,
    private val lastSuccessValues: BpField48LastSuccessValues,
) {

    suspend fun build(item: QueueItem): IsoMessage {
        val message = messageProvider.create().apply {
            mti = BpKeyConfig.REVERSE_MTI
            processingCode = item.processingCode
            amount = formatIsoAmount(item.amount)
            stan = item.stan
            dateTime = resolveDateTime(item)
            setRrn(item.rrn?.takeIf { it.isNotBlank() } ?: BpKeyConfig.REVERSE_DEFAULT_RRN)
            terminalId = item.terminalId
            currency = resolveCurrency(item.currency)
            if (item.type == TransactionType.BILL.ordinal) {
                item.adviceAdditionalData
                    ?.takeIf { it.isNotBlank() }
                    ?.let { additionalResponseData = it }
            }
            securityControlInfo = BpKeyConfig.FIELD53
            privateUseField63 = metadataProvider.metadata().toBpField63()
            setField48 {
                setField48Tag(
                    tag = BpKeyConfig.REVERSE_FIELD48_TAG,
                    value = lastSuccessValues.stanTagValue(),
                )
                setField48Tag(
                    tag = BpKeyConfig.REVERSE_FIELD48_TAG_CANCEL,
                    value = resolveCancelReason(item),
                )
            }
        }
        macCalculator.applyTransactionMac(message)
        return message
    }

    private fun resolveCancelReason(item: QueueItem): String {
        return item.reverseField48Tag21
            ?.takeIf { it.isNotBlank() }
            ?: BpCancelReasons.TIMEOUT
    }

    private fun resolveCurrency(currency: String): String {
        val digits = currency.filter(Char::isDigit)
        return digits.padStart(3, '0').takeLast(3)
            .ifBlank { BpKeyConfig.PURCHASE_CURRENCY }
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
