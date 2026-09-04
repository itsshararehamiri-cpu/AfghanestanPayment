package com.danesh.sadad.queue

import com.danesh.api.QueueItem
import com.danesh.api.TransactionIsoProfile
import com.danesh.api.TransactionType
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadReverseMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(item: QueueItem): IsoMessage {
        val session = messageSupport.beginSession()
        val functionCode = resolveFunctionCode(item)
        val pan = item.sourcePan?.filter { it.isDigit() }.orEmpty()
        val amount = item.amount.filter { it.isDigit() }.padStart(12, '0').takeLast(12)
        val stan = item.stan.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
        val originalRrn = item.rrn?.trim().orEmpty().takeIf { it.isNotBlank() }
        return messageProvider.create().apply {
            mti = SadadKeyConfig.REVERSE_MTI
            this.pan = pan
            processingCode = item.processingCode.filter { it.isDigit() }.padStart(6, '0').takeLast(6)
            this.amount = amount
            this.stan = stan
            nii = functionCode
            terminalId = item.terminalId
            merchantId = item.merchantId
            originalRrn?.let { setRrn(normalizeRrn(it)) }
            dateTime = session.dateTime
            setField48 {
                setTransactionType(functionCode)
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
        TransactionType.VOUCHER.ordinal -> SadadKeyConfig.VOUCHER_FUNCTION_CODE
        TransactionType.TOPUP.ordinal -> SadadKeyConfig.TOPUP_FUNCTION_CODE
        TransactionType.SUPPORT.ordinal -> SadadKeyConfig.SUPPORT_FUNCTION_CODE
        else -> "774"
    }

    private fun normalizeRrn(rrn: String): String {
        val digits = rrn.filter { it.isDigit() }
        return if (digits.length >= 12) digits.takeLast(12) else rrn.take(12).padStart(12, '0')
    }
}
