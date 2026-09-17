package com.danesh.bp.coupon.inquiry

import com.danesh.api.CouponListResult
import com.danesh.api.TransactionResultDetail

/**
 * مبلغ نقدی (فیلد 6)، شماره پیگیری کالابرگ (فیلد 44) و اعتبار تخصیص‌یافته به هر کالا
 * (فیلد 47) در [detail] (به‌ترتیب [TransactionResultDetail.couponCashAmount]،
 * [TransactionResultDetail.couponTrackingNumber] و [TransactionResultDetail.couponAssignedCredits])
 * توسط [com.danesh.bp.util.BpTransactionResultMapper] پر می‌شوند.
 */
class BpCouponInquiryResult(
    val detail: TransactionResultDetail,
) : CouponListResult(detail.isSuccess)
