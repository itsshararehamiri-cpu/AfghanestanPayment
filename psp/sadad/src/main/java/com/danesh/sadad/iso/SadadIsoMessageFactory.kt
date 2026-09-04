package com.danesh.sadad.iso

import com.danesh.api.TransactionContextProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadIsoMessageFactory @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) {
    fun terminalMerchantId(): String = contextProvider.getTerminalConfig().merchantId

    fun terminalMerchantName(): String = contextProvider.getTerminalConfig().merchantName

    fun merchantPhone(): String = contextProvider.getTerminalConfig().merchantPhone
}
