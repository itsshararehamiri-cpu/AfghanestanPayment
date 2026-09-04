package com.danesh.bp.cash_deposit

import com.danesh.api.PurchaseResult
import com.danesh.api.TransactionResultDetail

class BpCashDepositResult(
    val detail: TransactionResultDetail,
) : PurchaseResult(detail.isSuccess)
