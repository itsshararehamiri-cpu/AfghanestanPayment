package com.example.bill

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionResultDetail
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import com.example.bill.domain.BillPaymentUseCase
import com.example.bill.navigation.BillNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val billPaymentUseCase: BillPaymentUseCase,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(BillNavArgs.AMOUNT).orEmpty()
    private val billId: String =
        savedStateHandle.get<String>(BillNavArgs.BILL_ID).orEmpty()
    private val payId: String =
        savedStateHandle.get<String>(BillNavArgs.PAY_ID).orEmpty()
    private val requestId: String =
        savedStateHandle.get<String>(BillNavArgs.REQUEST_ID).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        return billPaymentUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,
            pan = pan,
            billId = billId,
            payId = payId,
            requestId = requestId,
        )
    }
}
