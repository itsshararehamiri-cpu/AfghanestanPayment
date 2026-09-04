package com.danesh.bp.purchase

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class BpPurchaseResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
