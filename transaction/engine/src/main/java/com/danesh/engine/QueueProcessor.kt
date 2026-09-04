package com.danesh.engine

import android.util.Log
import com.danesh.api.QueueRemovalPolicy
import com.danesh.common.RawMessage
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "QueueProcessor"

@Singleton
class QueueProcessor<M : RawMessage> @Inject constructor(
    private val store: TransactionStore<M>,
    private val adviceExecutor: QueueAdviceExecutor<M>,
    private val removalPolicy: QueueRemovalPolicy,
) {
    /**
     * صف SAF را پردازش می‌کند.
     *
     * - اگر رسید مشتری چاپ نشده باشد، به UI واگذار می‌شود (return false).
     * - اگر چاپ شده باشد، Advice (0220) یا Reverse (0400) ارسال می‌شود.
     *
     * @return false اگر رکوردی باقی بماند که باید بعداً دوباره تلاش شود.
     */
    suspend fun checkTxns(force: Boolean = true): Boolean {
        return flush()
    }

    suspend fun flush(): Boolean {
        val item = store.peekPending() ?: return true
        if (!item.customerReceiptPrinted) {
            return false
        }
        val result = adviceExecutor.executeAdvice(item)
        return if (removalPolicy.shouldRemoveFromQueue(result)) {
            store.clearQueue(item.date, item.time)
            flush()
        } else {
            false
        }
    }
}
