package com.danesh.bp.coupon.purchase

import com.danesh.api.TransactionResult
import com.danesh.api.TransactionResultDetail

class BpCouponPurchaseResult(
    val detail: TransactionResultDetail,
) : TransactionResult {
    override val isSuccess: Boolean get() = detail.isSuccess
}
