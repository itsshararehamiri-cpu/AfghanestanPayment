package com.danesh.hp.util

import com.danesh.common.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpTransactionMessages @Inject constructor(
    val appStrings: AppStrings,
) {
    fun queueFailed(): String = appStrings.txQueueFailed()

    fun connectFailed(): String = appStrings.txConnectFailed()

    fun sendFailed(): String = appStrings.txSendFailed()

    fun receiveFailed(): String = appStrings.txReceiveFailed()

    fun failed(): String = appStrings.txFailed()

    fun success(): String = appStrings.txSuccess()

    /** DE39=302 در پاسخ 1314 — بخش 9.3 مستند: پایانه نزد کارن پیکربندی نشده است. */
    fun notProvisioned(): String = appStrings.txNotProvisioned()
}
