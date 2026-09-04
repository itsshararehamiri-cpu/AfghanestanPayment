package com.danesh.hp.balance

import com.danesh.api.BalanceResult
import com.danesh.api.TransactionResultDetail

class HpBalanceResult(
    val detail: TransactionResultDetail,
) : BalanceResult(detail.isSuccess)
