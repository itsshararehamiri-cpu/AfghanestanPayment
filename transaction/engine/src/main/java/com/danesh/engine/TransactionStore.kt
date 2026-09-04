package com.danesh.engine

import com.danesh.api.QueueItem
import com.danesh.api.TransactionRequest
import com.danesh.common.RawMessage

interface TransactionStore<M : RawMessage> {

    /**
     * ثبت اولیه در SAF قبل از ارسال 0200 برای تراکنش قابل‌برگشت.
     * وضعیت اولیه همیشه [com.danesh.api.SafStatuses.NEEDS_REVERSE].
     */
    suspend fun registerSaf(message: M)

    /**
     * پس از پاسخ موفق سوئیچ: وضعیت را از R به S می‌برد تا Advice (0220) ارسال شود.
     */
    suspend fun confirmTxn(request: M, response: M? = null)

    /** حذف رکورد SAF (مثلاً وقتی 0200 هرگز ارسال نشد یا میزبان تراکنش را رد کرد). */
    suspend fun clearSaf(message: M)

    suspend fun saveReport(requestMessage: M, response: M? = null)

    /** اولین رکورد منتظر Advice یا Reverse. */
    suspend fun peekPending(): QueueItem?

    /** علامت‌گذاری چاپ/نمایش رسید مشتری برای رکورد SAF */
    suspend fun markCustomerReceiptPrinted(date: String, time: String)

    suspend fun delete(date: String, time: String)

    suspend fun clearQueue(date: String, time: String)
}
