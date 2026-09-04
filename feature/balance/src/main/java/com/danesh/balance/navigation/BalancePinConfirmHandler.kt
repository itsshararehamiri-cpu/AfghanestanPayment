//package com.danesh.balance.navigation
//
//import com.danesh.domain.BalanceTransactionUseCase
//import com.danesh.model.BalanceRequest
//import javax.inject.Inject
//
//class BalancePinConfirmHandler @Inject constructor(
//    private val balanceTransaction: BalanceTransactionUseCase
//) : PinConfirmHandler {
//
//    override suspend fun onPinConfirmed(
//        pinBlock: String,
//        cardData: String
//    ) {
//        balanceTransaction(
//            BalanceRequest(
//                pinBlock = pinBlock,
//                track2 = cardData
//            )
//        )
//    }
//}