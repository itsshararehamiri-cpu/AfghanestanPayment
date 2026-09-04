package com.danesh.wallet_to_wallet.model

data class WalletToWalletInput(
    val sourceWallet: String,
    val destinationWallet: String,
    val amount: Int,
)
