package com.danesh.voucher

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import com.danesh.voucher.domain.VoucherUseCase
import com.danesh.voucher.navigation.VoucherNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val voucherUseCase: VoucherUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(VoucherNavArgs.AMOUNT).orEmpty()

    private val operatorCode: String =
        savedStateHandle.get<String>(VoucherNavArgs.OPERATOR_CODE).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        return voucherUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,
            pan = pan,
            operatorCode = operatorCode,
        )
    }
}
