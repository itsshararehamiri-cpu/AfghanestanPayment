package com.danesh.sadad.queue

import com.danesh.api.AdviceReverseResult
import com.danesh.api.DefaultQueueRemovalPolicy
import com.danesh.api.QueueRemovalPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadQueueRemovalPolicy @Inject constructor() : QueueRemovalPolicy {
    override fun shouldRemoveFromQueue(result: AdviceReverseResult): Boolean {
        if (SadadSafResponseCodes.isSuccess(result.responseCode)) return true
        return DefaultQueueRemovalPolicy.shouldRemoveFromQueue(result)
    }
}
