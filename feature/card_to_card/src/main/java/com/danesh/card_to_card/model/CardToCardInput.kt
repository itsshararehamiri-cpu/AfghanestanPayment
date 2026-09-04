package com.danesh.card_to_card.model

data class CardToCardInput(
    val destination: String,
    val amount: Int,
    val destinationType: TransferDestinationType = TransferDestinationType.CARD,
) {
    val cardNumber: String get() = destination
    val isWallet: Boolean get() = destinationType == TransferDestinationType.WALLET
}
