package com.danesh.hp.purchase

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpPurchaseResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
