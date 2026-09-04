package com.danesh.engine

import com.danesh.api.AdviceReverseResult
import com.danesh.api.QueueItem
import com.danesh.common.RawMessage

interface QueueAdviceExecutor<M : RawMessage> {
    suspend fun executeAdvice(item: QueueItem): AdviceReverseResult
}
