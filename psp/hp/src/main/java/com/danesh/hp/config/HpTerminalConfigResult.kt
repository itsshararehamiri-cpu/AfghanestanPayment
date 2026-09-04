package com.danesh.hp.config

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail



class HpTerminalConfigResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
