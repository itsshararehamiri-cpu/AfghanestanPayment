package com.danesh.card_to_card.model

import androidx.annotation.DrawableRes
import com.danesh.card_to_card.R

data class CardToCardTransferDetails(
    val recipientName: String,
    val sourceCardNumber: String,
    val destinationNumber: String,
    val destinationIsWallet: Boolean = false,
    val amount: String,
    val currency: String,
    @DrawableRes val recipientAvatarRes: Int = R.drawable.bank,
) {
    val cardNumber: String get() = destinationNumber
}
