package com.danesh.api

interface QueueCustomerReceiptPrinter {
    suspend fun printCustomerReceipt(item: QueueItem): Boolean
}

interface QueueCustomerPrintMarker {
    suspend fun markCustomerReceiptPrinted(date: String, time: String)
}
