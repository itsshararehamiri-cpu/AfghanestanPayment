package com.danesh.sadad.toll

import com.danesh.api.TransactionRequest

/**
 * 25.1-INQUIRY TOLL PAYMENT — MTI 0100/0110، DE3 240000.
 * DE63 این تراکنش برخلاف بقیه، پیشوند Count ندارد:
 * FunctionCode(003, ثابت) + DataLength(n3) + OrganizationId(n3) + OrgDataLength(n3) + OrgData.
 * ساختار داخلیِ OrgData (کد ملی/نوع درخواست/نوع سفر/شناسه مرز) در جدول استخراج‌شده
 * از PDF ناخوانا بود؛ فراخوان باید این payload را از قبل بسازد.
 */
data class SadadTollInquiryRequest(
    val track2: String,
    val amount: String,
    /** پیش‌فرض سند: 090 */
    val organizationId: String = "090",
    val organizationData: String,
) : TransactionRequest

/**
 * 25.2-TOLL PAYMENT — MTI 0200/0210، DE3 730000.
 * طبق سند، DE48/DE61/DE63 دقیقاً همان داده‌های پاسخ مرحله‌ی Inquiry (0110) هستند
 * که باید بدون تغییر echo شوند.
 */
data class SadadTollPaymentRequest(
    val track2: String,
    val pinBlock: String,
    val amount: String,
    /** DE48 پاسخ Inquiry (Additional data). */
    val additionalData: String,
    /** DE61 پاسخ Inquiry (Private2) — اختیاری. */
    val private2Data: String = "",
    /** DE63 پاسخ Inquiry (Private4) — اختیاری. */
    val private4Data: String = "",
) : TransactionRequest
