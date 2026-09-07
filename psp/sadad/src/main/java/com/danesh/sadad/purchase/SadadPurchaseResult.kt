package com.danesh.sadad.purchase

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail



class SadadPurchaseResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)