package com.danesh.common.receipt

import com.danesh.api.QueueCustomerPrintMarker
import com.danesh.api.SafQueueFlusher
import com.danesh.api.TransactionResultDetail
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class QueueCustomerReceiptPrintTracker @Inject constructor(
    private val marker: QueueCustomerPrintMarker,
    private val safQueueFlusher: SafQueueFlusher,
) {
    fun markHandled(scope: CoroutineScope, result: TransactionResultDetail?) {
        val detail = result ?: return
        if (detail.date.isBlank() || detail.time.isBlank()) return
        scope.launch {
            marker.markCustomerReceiptPrinted(detail.date, detail.time)
            safQueueFlusher.flushAfterCustomerReceipt()
        }
    }
}
