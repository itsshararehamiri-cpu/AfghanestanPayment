package com.danesh.api

/**
 * پردازش صف SAF (Advice/Reverse) پس از نمایش/چاپ رسید مشتری.
 */
fun interface SafQueueFlusher {
    suspend fun flushAfterCustomerReceipt()
}
