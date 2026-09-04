package com.danesh.card_to_card

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.card_to_card.domain.CardToCardUseCase
import com.danesh.card_to_card.model.TransferDestinationType
import com.danesh.card_to_card.navigation.CardToCardNavArgs
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val cardToCardUseCase: CardToCardUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(CardToCardNavArgs.AMOUNT).orEmpty()
    private val destination: String =
        savedStateHandle.get<String>(CardToCardNavArgs.DESTINATION).orEmpty()
    private val destinationType: TransferDestinationType =
        TransferDestinationType.fromNav(
            savedStateHandle.get<String>(CardToCardNavArgs.DEST_TYPE).orEmpty(),
        )
    private val rrn: String =
        savedStateHandle.get<String>(CardToCardNavArgs.RRN).orEmpty()
    private val holderName: String =
        savedStateHandle.get<String>(CardToCardNavArgs.RECIPIENT_NAME).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        return cardToCardUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,
            pan = pan,
            destination = destination,
            destinationType = destinationType,
            rrn = rrn,
            holderName = holderName,
        )
    }
}
