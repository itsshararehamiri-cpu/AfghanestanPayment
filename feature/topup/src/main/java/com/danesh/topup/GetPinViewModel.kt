package com.danesh.topup

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import com.danesh.topup.domain.TopUpUseCase
import com.danesh.topup.navigation.TopUpNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val topUpUseCase: TopUpUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(TopUpNavArgs.AMOUNT).orEmpty()
    private val mobileNumber: String =
        savedStateHandle.get<String>(TopUpNavArgs.MOBILE).orEmpty()
    private val operatorCode: String =
        savedStateHandle.get<String>(TopUpNavArgs.OPERATOR_CODE).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        return topUpUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,
            pan = pan,
            mobileNumber = mobileNumber,
            operatorCode = operatorCode,
        )
    }
}
