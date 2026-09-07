package com.danesh.hp.card_to_wallet

import com.danesh.api.CardToWalletUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.hp.iso.HpIsoMessageSupport
import com.danesh.hp.key.HpKeyConfig
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import org.jpos.iso.ISOUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Card-to-Wallet Transfer (همراه‌پی)
 * MTI 1100 / DE3 500000 / Function Code 781
 * DE48: 002=781 ، 045=Wallet Code ، 049=Wallet Holder Name (از Name Inquiry)
 */
@Singleton
class HpCardToWalletMessageBuilder @Inject constructor(
    private val messageSupport: HpIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CardToWalletUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.CARD_TO_WALLET
        val walletCode = request.walletCode.filter { it.isDigit() }.take(8)
        val track2 = messageSupport.normalizeTrack2(request.track2)
        val rrn = request.rrn.trim().ifBlank { session.dateTime }
        val holderName = request.holderName.trim()

        return messageProvider.create().apply {
            mti = profile.mti
            processingCode = profile.processingCode
            stan = messageSupport.nextStan()
            pan = messageSupport.resolvePan(request.pan, request.track2)
            amount = request.amount
            dateTime = session.dateTime
           // messageSupport.run { applyHpStandardTerminalFields() }
            pointOfServiceEntryMode = HpKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
           // messageSupport.run { applyHpFunctionCode(profile) }
            currency = session.currency.ifBlank { HpKeyConfig.CARDHOLDER_BILLING_CURRENCY }
          //  tt51 = HpKeyConfig.CARDHOLDER_BILLING_CURRENCY
            if (track2.isNotBlank()) {
                this.track2 = track2
            }
            pinBlock = ISOUtil.hex2byte(request.pinBlock)
            mac = profile.emptyMac
           //setRrn(rrn)
       //     messageSupport.run { applyHpTransferAcquirerFields(track2) }
            setField48 {
               setTransactionType(FUNCTION_CODE)
                setField48Tag(WALLET_TAG, walletCode)
                if (holderName.isNotBlank()) {
                    setField48Tag(HOLDER_NAME_TAG, holderName)
                }
            }
        }
    }

    companion object {
        private const val FUNCTION_CODE = "781"
        private const val WALLET_TAG = "045"
        private const val HOLDER_NAME_TAG = "049"
    }
}
