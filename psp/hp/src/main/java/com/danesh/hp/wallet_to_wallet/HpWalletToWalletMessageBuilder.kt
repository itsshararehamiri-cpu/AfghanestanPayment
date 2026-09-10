package com.danesh.hp.wallet_to_wallet

import com.danesh.api.TransactionIsoProfile
import com.danesh.api.WalletToWalletUserInput
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HpWalletToWalletMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
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
            mti = profile.mti
            processingCode = profile.processingCode
            stan = messageSupport.nextStan()
            pan = sourceWallet
            amount = request.amount
            dateTime = session.dateTime
            messageSupport.run { applyHpFunctionCode(profile) }
            currency = session.currency.ifBlank { HpKeyConfig.CARDHOLDER_BILLING_CURRENCY }
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = profile.emptyMac
            messageSupport.run { applyHpAcceptorIds() }
            setRrn(rrn)
            setField48 {
                setField48Tag("004", "000")
                setTerminalType("2")
                setFinancialTransactionIndicator("1")
                setCard2NNumber(destinationWallet)
                if (holderName.isNotBlank()) {
                    setField48Tag(HOLDER_NAME_TAG, holderName)
                }
            }
        }
    }

    companion object {
        private const val HOLDER_NAME_TAG = "049"
    }
}
