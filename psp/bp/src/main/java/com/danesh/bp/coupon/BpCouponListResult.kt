package com.danesh.bp.coupon

import com.danesh.api.CouponListResult
import com.danesh.api.TransactionResultDetail

/**
 * نتیجه‌ی هر صفحه از لیست کالابرگ.
 * [rawList] متن خام فیلد 47 همین صفحه است (بدون هیچ پردازشی) — چون طبق مستند هر بخش
 * لیست ممکن است در وسط یک آیتم بریده شده باشد و مستقل قابل استفاده نیست، صرفاً باید
 * صفحات پشت‌سرهم (بدون جداکننده‌ی اضافه) به هم چسبانده شوند؛ این کار در
 * [com.danesh.bp.BpGateway.getCouponList] انجام می‌شود.
 * [lastIndex] مقدار تگ 1E فیلد 48 پاسخ (شماره آخرین اندیس لیست) است.
 */
class BpCouponListResult(
    val detail: TransactionResultDetail,
    val rawList: String = "",
    val requestedIndex: Int = 0,
    val lastIndex: Int = 0,
) : CouponListResult(detail.isSuccess)
