package com.danesh.hp.bill

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpBillPaymentResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
