package com.danesh.bp.voucher

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class BpVoucherResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
class BpTopUpResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
