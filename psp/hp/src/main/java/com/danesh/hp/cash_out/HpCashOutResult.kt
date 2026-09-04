package com.danesh.hp.cash_out

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpCashOutResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
