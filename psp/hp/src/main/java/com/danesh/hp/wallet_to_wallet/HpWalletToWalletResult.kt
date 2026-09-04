package com.danesh.hp.wallet_to_wallet

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpWalletToWalletResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
