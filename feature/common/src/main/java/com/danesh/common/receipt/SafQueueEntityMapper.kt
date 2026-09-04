package com.danesh.common.receipt

import com.danesh.api.QueueItem
import com.danesh.api.QueueOperations
import com.danesh.api.SafStatuses
import com.danesh.database.entity.StoreForwardQueueEntity

internal fun StoreForwardQueueEntity.toQueueItem(): QueueItem {
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
        reverseField48Tag21 = reverseDestValue.takeIf { reverseDestTag == "021" },
        sourcePan = sourcePan,
        functionCode = functionCode,
        reverseDestTag = reverseDestTag,
        reverseDestValue = reverseDestValue,
        status = effectiveStatus,
        queueOperation = QueueOperations.fromSafStatus(effectiveStatus),
    )
}

internal fun isPendingSafStatus(status: Char): Boolean =
    SafStatuses.needsReverse(status) ||
        SafStatuses.needsAdvice(status) ||
        status == 'P'
