package com.danesh.bp.init

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class BpInitResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
