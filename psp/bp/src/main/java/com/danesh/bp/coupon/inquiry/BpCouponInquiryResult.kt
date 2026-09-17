package com.danesh.bp.coupon.inquiry

import com.danesh.api.CouponListResult
import com.danesh.api.TransactionResultDetail

class BpCouponInquiryResult(
    val detail: TransactionResultDetail,    val commodities: List<CouponCommodity> = emptyList(),

    ) : CouponListResult(detail.isSuccess)
data class CouponCommodity(val id: String)
