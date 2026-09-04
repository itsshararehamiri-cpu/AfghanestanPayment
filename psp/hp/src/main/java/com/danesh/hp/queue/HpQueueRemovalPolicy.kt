package com.danesh.hp.queue

import com.danesh.api.AdviceReverseResult
import com.danesh.api.DefaultQueueRemovalPolicy
import com.danesh.api.QueueRemovalPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpQueueRemovalPolicy @Inject constructor() : QueueRemovalPolicy {

    override fun shouldRemoveFromQueue(result: AdviceReverseResult): Boolean =
        DefaultQueueRemovalPolicy.shouldRemoveFromQueue(result)
}
