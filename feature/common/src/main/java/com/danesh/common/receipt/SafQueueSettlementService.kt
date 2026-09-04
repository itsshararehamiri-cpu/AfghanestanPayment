package com.danesh.common.receipt

import com.danesh.api.SafQueueFlusher
import com.danesh.api.SafQueueReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * تسویه دستی صف SAF از منوی «تسویه با مرکز»:
 * Advice/Reverse برای رکوردهای چاپ‌شده؛ رسید معلق برای چاپ‌نشده‌ها.
 */
@Singleton
class SafQueueSettlementService @Inject constructor(
    private val safQueueReader: SafQueueReader,
    private val safQueueFlusher: SafQueueFlusher,
) {
    private val _refreshPendingReceipt = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshPendingReceipt: SharedFlow<Unit> = _refreshPendingReceipt.asSharedFlow()

    suspend fun hasPendingTransactions(): Boolean =
        safQueueReader.listAllPending().isNotEmpty()

    suspend fun settleWithCenter() {
        safQueueFlusher.flushAfterCustomerReceipt()
        _refreshPendingReceipt.tryEmit(Unit)
    }
}
