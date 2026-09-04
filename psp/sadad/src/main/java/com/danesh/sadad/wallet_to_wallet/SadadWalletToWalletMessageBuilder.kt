package com.danesh.sadad.wallet_to_wallet

import com.danesh.api.TransactionIsoProfile
import com.danesh.api.WalletToWalletUserInput
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadWalletToWalletMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: WalletToWalletUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.WALLET_TO_WALLET
        val sourceWallet = request.pan.filter { it.isDigit() }.take(16)
        val destinationWallet = request.destinationWallet.filter { it.isDigit() }.take(16)
        val rrn = request.rrn.trim().ifBlank { session.dateTime }
        val holderName = request.holderName.trim()
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = profile,
                    session = session,
                    pan = sourceWallet,
                    amount = request.amount,
                    track2 = request.track2,
                    pinBlock = request.pinBlock,
                    includeTrack2 = false,
                )
            }
            pan = sourceWallet
            currency = session.currency.ifBlank { SadadKeyConfig.CARDHOLDER_BILLING_CURRENCY }
            setRrn(rrn)
            setField48 {
                setField48Tag("004", "000")
                setTerminalType("2")
                setFinancialTransactionIndicator("1")
                setCard2NNumber(destinationWallet)
                if (holderName.isNotBlank()) {
                    setField48Tag(SadadKeyConfig.HOLDER_NAME_TAG, holderName)
                }
            }
        }
    }
}
