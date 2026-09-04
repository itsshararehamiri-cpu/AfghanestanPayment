package com.danesh.support

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.danesh.api.TransactionContextProvider
import com.danesh.api.TransactionResultDetail
import com.danesh.api.VatPercentageRules
import com.danesh.common.card.CardSession
import com.danesh.common.pin.BaseGetPinViewModel
import com.danesh.core.Device
import com.danesh.support.domain.SupportUseCase
import com.danesh.support.navigation.SupportNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetPinViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    device: Device,
    cardSession: CardSession,
    @ApplicationContext context: Context,
    private val supportUseCase: SupportUseCase,
    private val contextProvider: TransactionContextProvider,
) : BaseGetPinViewModel(savedStateHandle, device, cardSession, context) {

    private val amount: String =
        savedStateHandle.get<String>(SupportNavArgs.AMOUNT).orEmpty()
    private val serviceId: String =
        savedStateHandle.get<String>(SupportNavArgs.SERVICE_ID).orEmpty()
    private val title: String =
        savedStateHandle.get<String>(SupportNavArgs.TITLE).orEmpty()

    override suspend fun executeTransaction(
        pinBlock: String,
        track2: String,
        pan: String,
    ): TransactionResultDetail {
        val result = supportUseCase(
            pinBlock = pinBlock,
            track2 = track2,
            amount = amount,
            pan = pan,
            serviceId = serviceId,
        )
        if (result.isSuccess && VatPercentageRules.isVatConfigurationItem(title)) {
            contextProvider.saveVatPercentage(VatPercentageRules.normalize(amount))
        }
        return result
    }
}
