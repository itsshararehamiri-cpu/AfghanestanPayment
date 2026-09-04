package com.danesh.bp.cash_out

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class BpCashOutResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
