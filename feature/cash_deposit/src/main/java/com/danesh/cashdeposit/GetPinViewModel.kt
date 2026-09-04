package com.danesh.cashdeposit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.cashdeposit.domain.CashDepositUseCase
import com.danesh.cashdeposit.navigation.CashDepositNavArgs
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
    private val cashDepositUseCase: CashDepositUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(CashDepositNavArgs.AMOUNT).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,pan: String
    ): TransactionResultDetail {
        return cashDepositUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount, pan = pan
        )
    }
}
