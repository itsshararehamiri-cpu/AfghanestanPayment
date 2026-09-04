package com.danesh.hp.iso

import com.danesh.api.TransactionContextProvider
import javax.inject.Inject
import javax.inject.Singleton

/** متادیتای ترمینال HP — ساخت پیام ISO در builderهای اختصاصی هر تراکنش انجام می‌شود. */
@Singleton
class HpIsoMessageFactory @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) {
    fun terminalMerchantId(): String = contextProvider.getTerminalConfig().merchantId

    fun terminalMerchantName(): String = contextProvider.getTerminalConfig().merchantName

    fun merchantPhone(): String = contextProvider.getTerminalConfig().merchantPhone
}
