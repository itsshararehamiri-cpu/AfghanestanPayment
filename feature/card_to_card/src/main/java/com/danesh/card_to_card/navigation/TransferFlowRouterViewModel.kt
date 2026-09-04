package com.danesh.card_to_card.navigation

import androidx.lifecycle.ViewModel
import com.danesh.api.TransferFlowPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransferFlowRouterViewModel @Inject constructor(
    transferFlowPolicy: TransferFlowPolicy,
) : ViewModel() {
    val requiresNameInquiry: Boolean = transferFlowPolicy.requiresNameInquiry
    val supportsWalletTransfer: Boolean = transferFlowPolicy.supportsWalletTransfer
}
