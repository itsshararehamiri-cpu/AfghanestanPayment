package com.danesh.hp.init

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class HpInitResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
