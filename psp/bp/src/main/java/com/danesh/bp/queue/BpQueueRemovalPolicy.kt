package com.danesh.bp.queue

import com.danesh.api.AdviceReverseResult
import com.danesh.api.DefaultQueueRemovalPolicy
import com.danesh.api.QueueRemovalPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BpQueueRemovalPolicy @Inject constructor() : QueueRemovalPolicy {

    override fun shouldRemoveFromQueue(result: AdviceReverseResult): Boolean {
       return DefaultQueueRemovalPolicy.shouldRemoveFromQueue(result)
    }
}
