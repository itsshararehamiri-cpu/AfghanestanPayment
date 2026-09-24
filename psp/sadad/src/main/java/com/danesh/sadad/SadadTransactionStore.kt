package com.danesh.sadad

import android.util.Log
import com.danesh.api.IsoResponseCodes
import com.danesh.api.QueueItem
import com.danesh.api.QueueOperations
import com.danesh.api.SafStatuses
import com.danesh.api.TransactionSessionClock
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import com.danesh.api.parseTransactionClockFromField12
import com.danesh.database.cleanup.TransactionReportStorageCleanup
import com.danesh.database.dao.StoreForwardQueueDao
import com.danesh.database.dao.TransactionReportDao
import com.danesh.database.entity.StoreForwardQueueEntity
import com.danesh.database.entity.TransactionReportEntity
import com.danesh.engine.TransactionStore
import com.danesh.iso.IsoMessage
import com.danesh.sadad.key.SadadKeyConfig
import com.danesh.sadad.util.CardTrackUtils
import com.danesh.sadad.voucher.SadadChargeField62Parser
import com.danesh.sadad.voucher.SadadChargePinCipher
import com.danesh.sadad.voucher.SadadVoucherPinProtector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadTransactionStore @Inject constructor(
    private val queueDao: StoreForwardQueueDao,
    private val reportDao: TransactionReportDao,
    private val reportStorageCleanup: TransactionReportStorageCleanup,
    private val sessionClock: TransactionSessionClock,
    private val voucherPinProtector: SadadVoucherPinProtector,
    private val chargePinCipher: SadadChargePinCipher,
) : TransactionStore<IsoMessage> {

    override suspend fun registerSaf(message: IsoMessage) {
        insertQueueItem(
            message = message,
            response = null,
            status = SafStatuses.NEEDS_REVERSE,
        )
    }

    override suspend fun confirmTxn(request: IsoMessage, response: IsoMessage?) {
        withContext(Dispatchers.IO) {
            val (date, time) = resolveDateTime(request)
            queueDao.confirmByDateTime(
                status = SafStatuses.NEEDS_ADVICE,
                queueOperation = QueueOperations.ADVICE,
                rrn = response?.rrn ?: request.rrn,
                date = date,
                time = time,
            )
        }
    }

    override suspend fun clearSaf(message: IsoMessage) {
        withContext(Dispatchers.IO) {
            val (date, time) = resolveDateTime(message)
            queueDao.deleteByDateTime(date, time)
        }
    }

    override suspend fun saveReport(request: IsoMessage, response: IsoMessage?) {
        withContext(Dispatchers.IO) {
            reportStorageCleanup.cleanupIfNeeded()
            val (date, time) = resolveDateTime(request)
            val (destTag, destValue) = resolveReverseDestination(request)
            val chargePins = request.processingCode
                .takeIf { it == SadadKeyConfig.VOUCHER_PROCESSING_CODE }
                ?.takeIf { IsoResponseCodes.isApproved(response?.responseCode) }
                ?.let {
                    SadadChargeField62Parser.parse(response?.privateUseField62)
                        ?: SadadChargeField62Parser.parse(response?.privateUseField62Bytes())
                }
            reportDao.insert(
                TransactionReportEntity(
                    id = 0,
                    timestamp = System.currentTimeMillis(),
                    processingCode = request.processingCode,
                    amount = request.amount.toLongOrNull() ?: 0L,
                    stan = request.stan,
                    dateTransaction = date,
                    timeTransaction = time,
                    merchantId = request.merchantId,
                    maskedPan = resolveMaskedPan(request),
                    type = messageToType(request),
                    rrn = response?.rrn ?: request.rrn,
                    issuer = "",
                    responseCode = response?.responseCode?.toIntOrNull() ?: 0,
                    responseMsg = null,
                    destinationPan = destValue.takeIf { destTag == DEST_TAG_CARD },
                    walletCode = destValue.takeIf { destTag == DEST_TAG_WALLET },
                    terminalId = request.terminalId,
                    serialVoucher = chargePins?.serial?.takeIf { it.isNotBlank() },
                    pinVoucher = chargePins
                        ?.let(chargePinCipher::reveal)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { pin ->
                            Log.d(TAG, "7) store encrypt input=$pin")
                            val encrypted = voucherPinProtector.encryptToHex(pin).ifBlank { pin }
                            Log.d(TAG, "7) store encrypt output=$encrypted")
                            encrypted
                        }
                        ?: run {
                            Log.d(TAG, "7) encrypt skipped; plaintext pin blank")
                            null
                        },
                    serviceDesc = chargePins?.ussd?.takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    override suspend fun peekPending(): QueueItem? {
        return withContext(Dispatchers.IO) {
            queueDao.getAll()
                ?.filter { isPendingSafStatus(it.status) }
                ?.minByOrNull { it.dateTime }
                ?.toQueueItem()
        }
    }

    override suspend fun markCustomerReceiptPrinted(date: String, time: String) {
        withContext(Dispatchers.IO) {
            queueDao.updateCustomerReceiptPrintStatus(true, date, time)
        }
    }

    override suspend fun delete(date: String, time: String) {
        withContext(Dispatchers.IO) {
            queueDao.deleteByDateTime(date, time)
        }
    }

    override suspend fun clearQueue(date: String, time: String) {
        withContext(Dispatchers.IO) {
            queueDao.deleteByDateTime(date, time)
        }
    }

    private suspend fun insertQueueItem(
        message: IsoMessage,
        response: IsoMessage?,
        status: Char,
    ) {
        withContext(Dispatchers.IO) {
            val (date, time) = resolveDateTime(message)
            val dateTime = "$date$time"
            val (destTag, destValue) = resolveReverseDestination(message)
            queueDao.insert(
                StoreForwardQueueEntity(
                    dateTime = dateTime,
                    date = date,
                    time = time,
                    status = status,
                    customerReceiptPrinted = false,
                    processingCode = message.processingCode,
                    amount = message.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12),
                    stan = message.stan,
                    merchantId = message.merchantId,
                    maskedPan = resolveMaskedPan(message),
                    type = messageToType(message),
                    rrn = response?.rrn ?: message.rrn,
                    issuer = "",
                    responseCode = null,
                    responseMsg = null,
                    terminalId = message.terminalId,
                    posConditionCode = message.pointOfServiceEntryMode,
                    currency = message.currency,
                    queueOperation = QueueOperations.fromSafStatus(status),
                    sourcePan = resolveSourcePan(message),
                    functionCode = resolveFunctionCode(message),
                    reverseDestTag = destTag,
                    reverseDestValue = destValue,
                    track2 = message.track2.takeIf { it.isNotBlank() },
                    originalField63 = message.privateUseField63.takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private fun resolveFunctionCode(message: IsoMessage): String? {
        message.nii.filter { it.isDigit() }.takeIf { it.length == 3 }?.let { return it }
        message.unpackField48()
        return message.getField48Tag("002")
            ?.filter { it.isDigit() }
            ?.takeIf { it.length == 3 }
    }

    private fun resolveSourcePan(message: IsoMessage): String? {
        val raw = message.pan.ifBlank { CardTrackUtils.extractPan(message.track2) }
        return raw.filter { it.isDigit() }.takeIf { it.isNotBlank() }
    }

    private fun resolveReverseDestination(message: IsoMessage): Pair<String?, String?> {
        message.unpackField48()
        val cardDest = message.getField48Tag(DEST_TAG_CARD)?.filter { it.isDigit() }
        if (!cardDest.isNullOrBlank()) return DEST_TAG_CARD to cardDest
        val walletDest = message.getField48Tag(DEST_TAG_WALLET)?.filter { it.isDigit() }
        if (!walletDest.isNullOrBlank()) return DEST_TAG_WALLET to walletDest
        return null to null
    }

    private fun isPendingSafStatus(status: Char): Boolean =
        SafStatuses.needsReverse(status) ||
            SafStatuses.needsAdvice(status) ||
            status == 'P'

    private fun messageToType(message: IsoMessage): Int = when (message.processingCode) {
        "000000" -> TransactionType.PURCHASE.ordinal
        "310000" -> TransactionType.BALANCE.ordinal
        "210000" -> TransactionType.CASH_DEPOSIT.ordinal
        "010000" -> TransactionType.CASH_OUT.ordinal
        SadadKeyConfig.VOUCHER_PROCESSING_CODE -> TransactionType.VOUCHER.ordinal
        SadadKeyConfig.TOPUP_PROCESSING_CODE -> TransactionType.TOPUP.ordinal
        SadadKeyConfig.BILL_PROCESSING_CODE -> TransactionType.BILL.ordinal
        SadadKeyConfig.SUPPORT_PROCESSING_CODE -> TransactionType.SUPPORT.ordinal
        "500000" -> when (message.nii) {
            "689" -> TransactionType.CARD_TO_CARD.ordinal
            "781" -> TransactionType.CARD_TO_WALLET.ordinal
            else -> TransactionType.BILL.ordinal
        }
        "400000" -> when (message.nii) {
            "785" -> TransactionType.WALLET_TO_WALLET.ordinal
            else -> TransactionType.BALANCE.ordinal
        }
        else -> TransactionType.BALANCE.ordinal
    }

    private fun resolveMaskedPan(message: IsoMessage): String? {
        val rawPan = message.pan.ifBlank {
            CardTrackUtils.extractPan(message.track2)
        }
        return rawPan.maskPanForDisplay().takeIf { it.isNotBlank() }
    }

    private fun resolveDateTime(message: IsoMessage): Pair<String, String> {
        sessionClock.current()?.let { return it.date to it.time }
        parseTransactionClockFromField12(message.dateTime)?.let { return it.date to it.time }
        return "" to ""
    }

    private fun StoreForwardQueueEntity.toQueueItem(): QueueItem {
        val effectiveStatus = when (status) {
            'P' -> if (queueOperation == QueueOperations.ADVICE) {
                SafStatuses.NEEDS_ADVICE
            } else {
                SafStatuses.NEEDS_REVERSE
            }
            else -> status
        }
        return QueueItem(
            date = date,
            time = time,
            dateTime = dateTime,
            type = type,
            processingCode = processingCode,
            amount = amount,
            stan = stan,
            merchantId = merchantId,
            terminalId = terminalId,
            currency = currency,
            posConditionCode = posConditionCode,
            maskedPan = maskedPan,
            rrn = rrn,
            issuer = issuer,
            responseCode = responseCode,
            responseMsg = responseMsg,
            customerReceiptPrinted = customerReceiptPrinted,
            reverseField48Tag21 = reverseDestValue.takeIf { reverseDestTag == DEST_TAG_CARD },
            sourcePan = sourcePan,
            functionCode = functionCode,
            reverseDestTag = reverseDestTag,
            reverseDestValue = reverseDestValue,
            track2 = track2,
            originalField63 = originalField63,
            status = effectiveStatus,
            queueOperation = QueueOperations.fromSafStatus(effectiveStatus),
        )
    }

    companion object {
        private const val TAG = "sharjHoma"
        private const val DEST_TAG_CARD = "021"
        private const val DEST_TAG_WALLET = "045"
    }
}
