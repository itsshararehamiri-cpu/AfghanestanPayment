package com.danesh.engine

import com.danesh.api.TransactionRequest
import com.danesh.api.TransactionResult
import com.danesh.common.RawMessage

abstract class HandlerTransaction<Req : TransactionRequest, Res : TransactionResult, M : RawMessage> {
    abstract val needReport: Boolean
    abstract val isReversible: Boolean
    /** پس از پاسخ موفق، confirmTxn وضعیت را R→S می‌کند تا Advice ارسال شود. */
    open val needAdvice: Boolean get() = false
    /** Advice/Reverse پس از نمایش رسید مشتری در UI ارسال شود (نه بلافاصله پس از 0200). */
    open val deferAdviceUntilReceipt: Boolean get() = false
    /** پس از موفقیت، STAN/RRN برای تگ‌های 04/05 فیلد 48 ذخیره شود. */
    open val recordsLastSuccessReference: Boolean get() = true
    open val skipQueueFlush: Boolean get() = false

    abstract fun buildMessage(request: Req): M
    abstract fun queueFailure(request: Req): Res
    abstract fun isFailure(response: M?,): Boolean
    abstract fun failure(request: Req, sentMessage: M, response: M?): Res
    abstract fun success(request: Req, sentMessage: M, response: M?): Res
    abstract fun networkError(request: Req, sentMessage: M, e: Exception): Res

    open fun connectFailure(request: Req, sentMessage: M, error: Exception): Res =
        networkError(request, sentMessage, error)

    open fun sendFailure(request: Req, sentMessage: M, error: Exception): Res =
        networkError(request, sentMessage, error)

    open fun receiveFailure(request: Req, sentMessage: M, error: Exception): Res =
        networkError(request, sentMessage, error)
}
