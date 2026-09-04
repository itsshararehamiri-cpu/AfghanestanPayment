package com.danesh.cashout

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.cashout.domain.CashOutUseCase
import com.danesh.cashout.navigation.CashOutNavArgs
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
    private val cashOutUseCase: CashOutUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(CashOutNavArgs.AMOUNT).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,pan: String
    ): TransactionResultDetail {
        return cashOutUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,pan=pan
        )
    }
}
