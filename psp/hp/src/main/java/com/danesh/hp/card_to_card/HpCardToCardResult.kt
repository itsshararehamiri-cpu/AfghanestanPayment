package com.danesh.hp.card_to_card

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpCardToCardResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
