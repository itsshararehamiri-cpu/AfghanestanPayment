package com.danesh.engine

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ActiveTransactionGuard"

/**
 * Tracks in-flight ISO transactions so background SAF flush can pause while a txn runs.
 */
@Singleton
class ActiveTransactionGuard @Inject constructor() {

    private val activeCount = AtomicInteger(0)

    fun enter() {
        activeCount.incrementAndGet()
    }

    fun exit() {
        val remaining = activeCount.decrementAndGet()
        if (remaining < 0) {
            activeCount.set(0)
            Log.w(TAG, "exit() called without matching enter()")
        }
    }

    fun isActive(): Boolean = activeCount.get() > 0
}
