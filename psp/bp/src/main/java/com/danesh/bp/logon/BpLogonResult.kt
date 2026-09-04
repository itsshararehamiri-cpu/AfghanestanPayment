package com.danesh.bp.logon

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class BpLogonResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
