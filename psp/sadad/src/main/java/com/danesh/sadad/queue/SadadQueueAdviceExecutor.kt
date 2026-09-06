package com.danesh.sadad.queue

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
class SadadQueueAdviceExecutor @Inject constructor(
    private val executorProvider: Provider<TransactionExecutor<IsoMessage>>,
    private val adviceHandler: SadadAdviceHandler,
    private val reverseHandler: SadadReverseHandler,
) : QueueAdviceExecutor<IsoMessage> {

    override suspend fun executeAdvice(item: QueueItem): SadadAdviceResult {
        return withContext(Dispatchers.IO) {
            val executor = executorProvider.get()
            when {
                SafStatuses.needsAdvice(item.status) -> executor.execute(
                    request = SadadAdviceRequest(item),
                    handler = adviceHandler,
                )
                else -> executor.execute(
                    request = SadadReverseRequest(item),
                    handler = reverseHandler,
                )
            }
        }
    }
}
