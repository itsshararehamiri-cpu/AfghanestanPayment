package com.danesh.sadad.topup

import com.danesh.api.TopUpResult



import com.danesh.api.BalanceResult
import com.danesh.api.TransactionResultDetail

class SadadTopUpResult(
    val detail: TransactionResultDetail,
) : TopUpResult(detail.isSuccess)
