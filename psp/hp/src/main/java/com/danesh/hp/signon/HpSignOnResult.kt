package com.danesh.hp.signon

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class HpSignOnResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
