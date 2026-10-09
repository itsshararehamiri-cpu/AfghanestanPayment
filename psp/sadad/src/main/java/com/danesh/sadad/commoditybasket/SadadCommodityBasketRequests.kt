package com.danesh.sadad.commoditybasket

import com.danesh.api.CouponOrderItem
import com.danesh.api.TransactionRequest

/**
 * 15-INQUIRY COMMODITY BASKET (کالابرگ) — MTI 0100، DE3 680000، DE63 FC 046.
 * فقط کشیدن کارت لازم است (بدون رمز).
 */
data class SadadCommodityBasketInquiryRequest(
    val track2: String,
    val pan: String,
    val items: List<CouponOrderItem>,
    /** جمع (مبلغ × تعداد) اقلام — DE4. */
    val totalAmount: Long,
) : TransactionRequest

/**
 * 16-SALE COMMODITY BASKET — MTI 0200، DE3 680000، DE63 FC 047 (همان سه مقدار پاسخ استعلام). با رمز.
 */
data class SadadCommodityBasketSaleRequest(
    val track2: String,
    val pan: String,
    val pinBlock: String,
    val transactionAmount: Long,
    val creditAmount: Long,
    val traceItem: String,
) : TransactionRequest

/**
 * 17-CANCEL COMMODITY BASKET — MTI 0100، DE3 680000، DE63 FC 058 = شماره پیگیری استعلام.
 */
data class SadadCommodityBasketCancelRequest(
    val track2: String,
    val orderTraceId: String,
) : TransactionRequest
