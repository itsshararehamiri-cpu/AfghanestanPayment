package com.danesh.sadad.cash_out

import com.danesh.api.CashOutUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadCashOutMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CashOutUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = TransactionIsoProfile.CASH_OUT,
                    session = session,
                    pan = request.pan,
                    amount = request.amount,
                    track2 = request.track2,
                    pinBlock = request.pinBlock,
                )
            }
            setField48 {
                setTransactionType("700")
                setTerminalType("2")
                setCard2NNumber(request.destinationAccount)
                setFinancialTransactionIndicator("1")
            }
        }
    }
}
