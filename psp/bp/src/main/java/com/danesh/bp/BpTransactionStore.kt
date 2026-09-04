package com.danesh.bp

import android.util.Log
import com.danesh.api.QueueItem
import com.danesh.api.QueueOperations
import com.danesh.api.SafStatuses
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionSessionClock
import com.danesh.api.TransactionType
import com.danesh.api.maskPanForDisplay
import com.danesh.api.parseTransactionClockFromField12
import com.danesh.bp.key.BpKeyConfig
import com.danesh.bp.util.CardTrackUtils
import com.danesh.database.cleanup.TransactionReportStorageCleanup
import com.danesh.database.dao.StoreForwardQueueDao
import com.danesh.database.dao.TransactionReportDao
import com.danesh.database.entity.StoreForwardQueueEntity
import com.danesh.database.entity.TransactionReportEntity
import com.danesh.engine.TransactionStore
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpTransactionStore @Inject constructor(
    private val queueDao: StoreForwardQueueDao,
    private val reportDao: TransactionReportDao,
    private val reportStorageCleanup: TransactionReportStorageCleanup,
    private val sessionClock: TransactionSessionClock,
    private val contextProvider: TransactionContextProvider,
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
            val terminalConfig = contextProvider.getTerminalConfig()
            reportDao.insert(
                TransactionReportEntity(
                    id = 0,
                    timestamp = System.currentTimeMillis(),
                    processingCode = request.processingCode,
                    amount = request.amount.toLongOrNull() ?: 0L,
                    stan = request.stan,
                    dateTransaction = date,
                    timeTransaction = time,
                    merchantId = request.merchantId.ifBlank { terminalConfig.merchantId },
                    maskedPan = resolveMaskedPan(request),
                    type = processingCodeToType(request.processingCode),
                    rrn = response?.rrn ?: request.rrn,
                    issuer = "",
                    responseCode = response?.responseCode?.toIntOrNull() ?: 0,
                    responseMsg = null,
                    terminalId = request.terminalId.ifBlank { terminalConfig.terminalId },
                    merchantName = terminalConfig.merchantName,
                    merchantPhone = terminalConfig.merchantPhone.takeIf { it.isNotBlank() },
                    billId = request.getField48Tag(BpKeyConfig.BILL_ID_FIELD48_TAG), payId = request.getField48Tag(BpKeyConfig.PAY_ID_FIELD48_TAG)
                ),
            )
        }
    }

    override suspend fun peekPending(): QueueItem? {

        return withContext(Dispatchers.IO) {
            val v=queueDao.getAll()
                ?.filter { isPendingSafStatus(it.status) }
                ?.minByOrNull { it.dateTime }
                ?.toQueueItem()
            Log.d("TAG", "peekPending: dffjjhjhd$v")
            v
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
            queueDao.insert(
                StoreForwardQueueEntity(
                    dateTime = dateTime,
                    date = date,
                    time = time,
                    status = status,
                    customerReceiptPrinted = false,
                    processingCode = message.processingCode,
                    amount = message.amount,
                    stan = message.stan,
                    merchantId = message.merchantId,
                    maskedPan = resolveMaskedPan(message),
                    type = processingCodeToType(message.processingCode),
                    rrn = response?.rrn ?: message.rrn,
                    issuer = "",
                    responseCode = null,
                    responseMsg = null,
                    terminalId = message.terminalId,
                    posConditionCode = message.pointOfServiceEntryMode,
                    currency = message.currency,
                    queueOperation = QueueOperations.fromSafStatus(status),
                ),
            )
        }
    }

    private fun isPendingSafStatus(status: Char): Boolean =
        SafStatuses.needsReverse(status) ||
            SafStatuses.needsAdvice(status) ||
            // سازگاری با رکوردهای قدیمی که status='P' داشتند
            status == 'P'

    private fun processingCodeToType(processingCode: String): Int = when (processingCode) {
        "000000" -> TransactionType.PURCHASE.ordinal
        "310000" -> TransactionType.BALANCE.ordinal
        "210000" -> TransactionType.CASH_DEPOSIT.ordinal
        "010000" -> TransactionType.CASH_OUT.ordinal
        "150000" -> TransactionType.VOUCHER.ordinal
        "170000" -> TransactionType.BILL.ordinal
        "230000" -> TransactionType.TOPUP.ordinal
        "100000" -> TransactionType.SUPPORT.ordinal
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
            sourcePan = sourcePan,
            functionCode = functionCode,
            reverseDestTag = reverseDestTag,
            reverseDestValue = reverseDestValue,
            reverseField48Tag21 = reverseDestValue.takeIf { reverseDestTag == "021" },
            status = effectiveStatus,
            queueOperation = QueueOperations.fromSafStatus(effectiveStatus),
        )
    }
}
