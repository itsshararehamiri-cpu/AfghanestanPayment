package com.danesh.sadad.commoditybasket

import com.danesh.api.TransactionRequest

/**
 * 15-INQUIRY COMMODITY BASKET (کالابرگ) — MTI 0100/0110، DE3 680000.
 * DE63 (Function Code 046) باید شامل «تعداد اقلام + [بارکد،واحد،تعداد] برای هر قلم»
 * باشد؛ عرض دقیق هر زیرفیلد در جدول استخراج‌شده از PDF ناخوانا/متناقض بود (نمونه‌ی
 * سند: «01046045002661998817831701000100665546334485301000200»), بنابراین این
 * builder بدنه‌ی TLV بیرونی (Count/FunctionCode/#len) را می‌سازد و payload دقیق
 * اقلام را از لایه‌ی بارکدخوان/UI می‌گیرد — قبل از استفاده‌ی واقعی با دادهٔ نمونه‌ی
 * سوئیچ تطبیق داده شود.
 */
data class SadadCommodityBasketInquiryRequest(
    /** payload از قبل ساخته‌شده‌ی اقلام سبد کالا (بدون Count/FunctionCode/#len). */
    val commodityListData: String,
) : TransactionRequest

/**
 * 16-SALE COMMODITY BASKET — MTI 0200/0210، DE3 680000.
 * DE63 (Function Code 047) دقیقاً همان سه مقدار پاسخِ مرحله‌ی Inquiry را echo می‌کند:
 * Transaction amount(n12) + Credit amount(n12) + Trace item(n13).
 */
data class SadadCommodityBasketSaleRequest(
    val transactionAmount: String,
    val creditAmount: String,
    val traceItem: String,
) : TransactionRequest

/**
 * 17-CANCEL COMMODITY BASKET — MTI 0100/0110، DE3 680000.
 * DE63 (Function Code 058) = OrderTraceId (n13) از مرحله‌ی Inquiry.
 */
data class SadadCommodityBasketCancelRequest(
    val orderTraceId: String,
) : TransactionRequest
