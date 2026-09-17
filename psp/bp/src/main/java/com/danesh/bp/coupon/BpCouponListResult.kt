package com.danesh.bp.coupon

import com.danesh.api.CouponListResult
import com.danesh.api.TransactionResultDetail

class BpCouponListResult(
    val detail: TransactionResultDetail,    val commodities: List<CouponCommodity> = emptyList(),

    ) : CouponListResult(detail.isSuccess)
data class CouponCommodity(val id: String)
