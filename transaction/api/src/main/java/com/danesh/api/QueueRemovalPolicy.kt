package com.danesh.api

/**
 * Defines when a store-and-forward queue item should be removed after an advice attempt.
 * Each PSP can provide its own implementation.
 */
fun interface QueueRemovalPolicy {
    fun shouldRemoveFromQueue(result: AdviceReverseResult): Boolean
}
