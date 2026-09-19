package com.danesh.sadad.refund

import com.danesh.api.TransactionRequest

/**
 * 20-REFUND (MTI 0200/0210، DE3 200000).
 * DE61 = RefundType(n2) + Reference(n12) + Amount(n12) + SystemTrace(n6).
 */
data class SadadRefundRequest(
    /** "01" آفلاین یا "02" آنلاین. */
    val refundType: String,
    /** مرجع تراکنش اصلی (۱۲ رقم). */
    val reference: String,
    /** مبلغ استرداد (بدون صفرهای اضافه؛ در DE04 و DE61 استفاده می‌شود). */
    val amount: String,
    /** STAN تراکنش اصلی (۶ رقم). */
    val originalStan: String,
) : TransactionRequest
