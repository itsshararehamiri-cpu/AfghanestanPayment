package com.danesh.bp.bill

import com.danesh.api.BalanceResult
import com.danesh.api.TransactionResultDetail


class BpBillResult(
    val detail: TransactionResultDetail,
) : BalanceResult(detail.isSuccess)