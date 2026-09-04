package com.danesh.hp.logon

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class HpLogonResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
