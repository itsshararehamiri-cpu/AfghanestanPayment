package com.danesh.wallet_to_wallet.model

import androidx.annotation.DrawableRes
import com.danesh.common.R as CommonR

data class WalletToWalletTransferDetails(
    val recipientName: String,
    val sourceWalletNumber: String,
    val destinationWalletNumber: String,
    val amount: String,
    val currency: String,
    @DrawableRes val recipientAvatarRes: Int = CommonR.drawable.ic_pan,
)
