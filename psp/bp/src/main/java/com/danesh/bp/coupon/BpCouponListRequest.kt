package com.danesh.bp.coupon

import com.danesh.api.TransactionRequest

/**
 * درخواست داخلی هر صفحه از لیست کالابرگ. چون لیست ممکن است در چند تراکنش
 * (هر بار با اندیس بعدی) دریافت شود، این نوع مستقل از [com.danesh.api.CouponListUserInput]
 * نگه داشته می‌شود تا [com.danesh.bp.coupon.CouponListHandler] بتواند برای هر صفحه با
 * اندیس متفاوت اجرا شود؛ صفحه‌بندی در [com.danesh.bp.BpGateway.getCouponList] انجام می‌شود.
 */
data class BpCouponListRequest(
    val requestedIndex: Int,
) : TransactionRequest
