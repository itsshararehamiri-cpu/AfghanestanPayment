package com.danesh.sadad.balance



import com.danesh.api.BalanceResult
import com.danesh.api.TransactionResultDetail

class SadadBalanceResult(
    val detail: TransactionResultDetail,
) : BalanceResult(detail.isSuccess)
