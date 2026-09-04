package com.danesh.common.qr

import androidx.lifecycle.ViewModel
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionContextProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrPaymentConfigViewModel @Inject constructor(
    private val contextProvider: TransactionContextProvider,
) : ViewModel() {

    fun terminalConfig(): TerminalConfig = contextProvider.getTerminalConfig()
}
