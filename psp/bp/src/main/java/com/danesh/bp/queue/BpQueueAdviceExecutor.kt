package com.danesh.bp.queue

import com.danesh.api.QueueItem
import com.danesh.api.SafStatuses
import com.danesh.api.TransactionType
import com.danesh.bp.key.BpKeyConfig
import com.danesh.engine.QueueAdviceExecutor
import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BpQueueAdviceExecutor @Inject constructor(
    private val executorProvider: Provider<TransactionExecutor<IsoMessage>>,
    private val adviceHandler: BpAdviceHandler,
    private val reverseHandler: BpReverseHandler,
) : QueueAdviceExecutor<IsoMessage> {

    override suspend fun executeAdvice(item: QueueItem): BpAdviceResult {
        if (item.isTopUpWithoutSaf()) {
            return BpAdviceResult(isSuccess = true)
        }
        return withContext(Dispatchers.IO) {
            val executor = executorProvider.get()
            when {
                SafStatuses.needsAdvice(item.status) -> executor.execute(
                    request = BpAdviceRequest(item),
                    handler = adviceHandler,
                )
                else -> executor.execute(
                    request = BpReverseRequest(item),
                    handler = reverseHandler,
                )
            }
        }
    }

    private fun QueueItem.isTopUpWithoutSaf(): Boolean =
        processingCode == BpKeyConfig.TOPUP_PROCESSING_CODE ||
            type == TransactionType.TOPUP.ordinal
}
