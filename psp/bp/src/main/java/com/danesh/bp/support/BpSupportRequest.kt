package com.danesh.bp.support

import com.danesh.api.SupportUserInput
import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

typealias BpSupportRequest = SupportUserInput

class BpSupportResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
