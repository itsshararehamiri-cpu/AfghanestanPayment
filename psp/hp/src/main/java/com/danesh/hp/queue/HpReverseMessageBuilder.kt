package com.danesh.hp.queue

import android.util.Log
import com.danesh.api.QueueItem
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionType
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reversal Advice همراه‌پی — MTI 1420 / پاسخ 1430.
 *
 * از تراکنش اصلی: DE2, DE3, DE4, DE11, DE24, DE37, DE41, DE42, DE48
 * برای خود Reverse: DE12 (تاریخ/زمان جدید)
 */
@Singleton
class HpReverseMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        val session = messageSupport.beginSession()
        val functionCode = resolveFunctionCode(item)
        val pan = item.sourcePan?.filter { it.isDigit() }.orEmpty()
        val amount = item.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val stan = item.stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val originalRrn = item.rrn?.trim().orEmpty().takeIf { it.isNotBlank() }
        val destTag = item.reverseDestTag?.takeIf { it.isNotBlank() }
        val destValue = item.reverseDestValue?.takeIf { it.isNotBlank() }
            ?: item.reverseField48Tag21?.takeIf { it.isNotBlank() }
        return messageProvider.create().apply {
            mti = REVERSE_MTI
            // --- از تراکنش اصلی ---
            this.pan = pan
            processingCode = item.processingCode.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
            this.amount = amount
            this.stan = stan
            nii = functionCode
            terminalId = item.terminalId
            merchantId = item.merchantId
            originalRrn?.let { setRrn(normalizeRrn(it)) }
            // --- مخصوص همین Reverse ---
            dateTime = session.dateTime
            setField48 {
                setTransactionType(functionCode)
//                if (!destTag.isNullOrBlank() && !destValue.isNullOrBlank()) {
//                    when (destTag) {
//                        DEST_TAG_CARD -> setCard2NNumber(destValue.filter { it.isDigit() }.take(16))
//                        DEST_TAG_WALLET -> setField48Tag(
//                            DEST_TAG_WALLET,
//                            destValue.filter { it.isDigit() }.take(8),
//                        )
//                        else -> setField48Tag(destTag, destValue)
//                    }
//                }
            }
            mac = TransactionIsoProfile.BALANCE.emptyMac

        }
    }

    private fun resolveFunctionCode(item: QueueItem): String {
        item.functionCode?.filter { it.isDigit() }?.takeIf { it.length == 3 }?.let { return it }
        return defaultFunctionCode(item.type)
    }

    private fun defaultFunctionCode(type: Int): String = when (type) {
        TransactionType.PURCHASE.ordinal -> "774"
        TransactionType.BALANCE.ordinal -> "702"
        TransactionType.CASH_DEPOSIT.ordinal -> "618"
        TransactionType.CASH_OUT.ordinal -> "700"
        TransactionType.BILL.ordinal -> "508"
        TransactionType.CARD_TO_CARD.ordinal -> "689"
        TransactionType.CARD_TO_WALLET.ordinal -> "781"
        TransactionType.WALLET_TO_WALLET.ordinal -> "785"
        else -> "774"
    }

    private fun normalizeRrn(rrn: String): String {
        val digits = rrn.filter { it.isDigit() }
        return if (digits.length >= 12) digits.takeLast(12) else rrn.take(12).padStart(12, '0')
    }

    companion object {
        private const val REVERSE_MTI = "1420"
        private const val DEST_TAG_CARD = "021"
        private const val DEST_TAG_WALLET = "045"
    }
}
