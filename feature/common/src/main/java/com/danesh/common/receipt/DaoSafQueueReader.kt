package com.danesh.common.receipt

import android.util.Log
import com.danesh.api.QueueItem
import com.danesh.api.SafQueueReader
import com.danesh.database.dao.StoreForwardQueueDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DaoSafQueueReader @Inject constructor(
    private val queueDao: StoreForwardQueueDao,
) : SafQueueReader {

    override suspend fun peekFirstPending(): QueueItem? = withContext(Dispatchers.IO) {
        listAllPendingInternal().firstOrNull()
    }

    override suspend fun listAllPending(): List<QueueItem> = withContext(Dispatchers.IO) {
        listAllPendingInternal()
    }

    private fun listAllPendingInternal(): List<QueueItem>
        {
            val v=queueDao.getAll()
                .orEmpty()
                .filter { isPendingSafStatus(it.status) }
                .sortedByDescending { it.dateTime }
                .map { it.toQueueItem() }
val t=queueDao.getAll()
            Log.d("TAG", "getUnsettledTransactions: jjjhjjjhjtjxeid")
            t?.forEach {
                Log.d("TAG", "getUnsettledTransactions: jjjhjjjhjtjxeidf$it")

            }
            v?.forEach {
                Log.d("TAG", "getUnsettledTransactions: jjjhjjjhjtjxeidfv$it")

            }

            return v
        }
}
