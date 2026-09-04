package com.danesh.balance.navigation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.balance.domain.BalanceUseCase
import com.danesh.api.TransactionResultDetail
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
    private val balanceUseCase: BalanceUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,pan: String
    ): TransactionResultDetail {
        return balanceUseCase(pinBlock = pinBlock, track2 = track2,pan=pan)
    }
}
