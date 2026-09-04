package com.danesh.sadad.card_to_card

import com.danesh.api.CardToCardUserInput
import com.danesh.api.TransactionIsoProfile
import com.danesh.iso.IsoMessage
import com.danesh.iso.IsoMessageProvider
import com.danesh.sadad.iso.SadadIsoMessageSupport
import com.danesh.sadad.key.SadadKeyConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadCardToCardMessageBuilder @Inject constructor(
    private val messageSupport: SadadIsoMessageSupport,
    private val messageProvider: IsoMessageProvider,
) {
    fun build(request: CardToCardUserInput): IsoMessage {
        val session = messageSupport.beginSession()
        val profile = TransactionIsoProfile.CARD_TO_CARD
        val destinationPan = request.destinationPan.filter { it.isDigit() }.take(16)
        val track2 = messageSupport.normalizeTrack2(request.track2)
        val holderName = request.holderName.trim()
        return messageProvider.create().apply {
            messageSupport.run {
                applySadadCardFields(
                    profile = profile,
                    session = session,
                    pan = request.pan,
                    amount = request.amount,
                    track2 = request.track2,
                    pinBlock = request.pinBlock,
                    includeTrack2 = false,
                )
            }
            pointOfServiceEntryMode = SadadKeyConfig.CARD_TO_CARD_POS_ENTRY_MODE
            currency = session.currency.ifBlank { SadadKeyConfig.CARDHOLDER_BILLING_CURRENCY }
            messageSupport.run { applySadadTransferAcquirerFields(track2) }
            setField48 {
                setCard2NNumber(destinationPan)
                if (holderName.isNotBlank()) {
                    setField48Tag(SadadKeyConfig.HOLDER_NAME_TAG, holderName)
                }
            }
        }
    }
}
