package com.danesh.hp.card_to_wallet

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpCardToWalletResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
