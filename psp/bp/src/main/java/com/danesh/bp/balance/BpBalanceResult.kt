package com.danesh.bp.balance

import com.danesh.api.BalanceResult
import com.danesh.api.TransactionResultDetail

class BpBalanceResult(
    val detail: TransactionResultDetail,
) : BalanceResult(detail.isSuccess)
