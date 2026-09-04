package com.danesh.hp.queue

import com.danesh.api.QueueItem
import com.danesh.api.SafStatuses
import com.danesh.engine.QueueAdviceExecutor
import com.danesh.engine.TransactionExecutor
import com.danesh.iso.IsoMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class HpQueueAdviceExecutor @Inject constructor(
    private val executorProvider: Provider<TransactionExecutor<IsoMessage>>,
    private val reverseHandler: HpReverseHandler,
) : QueueAdviceExecutor<IsoMessage> {

    override suspend fun executeAdvice(item: QueueItem): HpAdviceResult {
        return withContext(Dispatchers.IO) {
            val executor = executorProvider.get()
            when {
                SafStatuses.needsAdvice(item.status) -> HpAdviceResult(
                    isSuccess = true,
                    responseCode = "000",
                )
                else -> executor.execute(
                    request = HpReverseRequest(item),
                    handler = reverseHandler,
                )
            }
        }
    }
}
