package com.danesh.sadad.cash_deposit

import com.danesh.api.CashDepositUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadCashDepositMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CashDepositUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = TransactionIsoProfile.CASH_DEPOSIT,
                    session = session,
                    pan = request.pan,
                    amount = request.amount,
                    track2 = request.track2,
                    pinBlock = request.pinBlock,
                )
            }
            setField48 {
                setTransactionType("618")
                setTerminalType("2")
                setCard2NNumber(request.destinationAccount)
                setFinancialTransactionIndicator("1")
            }
        }
    }
}
