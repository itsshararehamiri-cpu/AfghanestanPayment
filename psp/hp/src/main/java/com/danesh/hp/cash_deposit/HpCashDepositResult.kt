package com.danesh.hp.cash_deposit

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class HpCashDepositResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
