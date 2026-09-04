package com.danesh.common.receipt

import com.danesh.api.QueueCustomerPrintMarker
import com.danesh.database.dao.StoreForwardQueueDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Singleton
class DefaultQueueCustomerPrintMarker @Inject constructor(
    private val queueDao: StoreForwardQueueDao,
) : QueueCustomerPrintMarker {

    override suspend fun markCustomerReceiptPrinted(date: String, time: String) {
        if (date.isBlank() || time.isBlank()) return
        withContext(Dispatchers.IO) {
            queueDao.updateCustomerReceiptPrintStatus(true, date, time)
        }
    }
}
