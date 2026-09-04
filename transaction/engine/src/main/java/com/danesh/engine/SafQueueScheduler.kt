package com.danesh.engine

import android.util.Log
import com.danesh.common.RawMessage
import com.danesh.common.startup.AppStartupTask
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "SafQueueScheduler"

/**
 * هر ۲ دقیقه صف SAF را بررسی می‌کند (فقط وقتی تراکنش ISO در جریان نیست).
 * رکوردهای چاپ‌شده: Advice/Reverse در پس‌زمینه.
 * رکوردهای چاپ‌نشده: [PendingSafReceiptGate] در UI رسید را نمایش می‌دهد.
 *
 * علاوه بر این، قبل از هر تراکنش جدید نیز [QueueProcessor.checkTxns] صدا زده می‌شود.
 * حلقه در پس‌زمینه اجرا می‌شود تا بقیهٔ startup taskها بلاک نشوند.
 */
@Singleton
class SafQueueScheduler<M : RawMessage> @Inject constructor(
    private val queueProcessor: QueueProcessor<M>,
    private val activeTransactionGuard: ActiveTransactionGuard,
) : AppStartupTask {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)

    override suspend fun run() {
        if (!started.compareAndSet(false, true)) return
        flushQueueWhenIdle()
        scope.launch {
            while (true) {
                waitUntilIdle()
                delay(INTERVAL_MS)
                Log.d(TAG, "rddun: dddddddddddddddddhhhh")
                if (activeTransactionGuard.isActive()) continue
                flushQueueWhenIdle()
            }
        }
    }

    private suspend fun waitUntilIdle() {
        while (activeTransactionGuard.isActive()) {
            delay(BUSY_POLL_MS)
        }
    }

    private suspend fun flushQueueWhenIdle() {
        Log.d(TAG, "flushQueueWhenIdle: ddddddflushQueueWhenIdleddd")
        if (activeTransactionGuard.isActive()) return
        runCatching {
            queueProcessor.checkTxns(force = true)
        }.onFailure { error ->
            Log.w(TAG, "background SAF flush failed: ${error.message}", error)
        }
    }

    companion object {
        const val INTERVAL_MS = 120_000L
        private const val BUSY_POLL_MS = 500L
    }
}
