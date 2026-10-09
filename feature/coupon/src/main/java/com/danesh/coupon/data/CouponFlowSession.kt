package com.danesh.coupon.data

import com.danesh.api.CouponOrderItem
import com.danesh.api.CouponProduct
import com.danesh.api.TransactionResultDetail
import javax.inject.Inject
import javax.inject.Singleton

/** یک ردیف سبد: کالا + مبلغ (ریال) + تعداد. مبلغ کل ردیف = مبلغ × تعداد. */
data class CouponCartLine(
    val id: Long,
    val product: CouponProduct,
    val amountDigits: String = "",
    val count: Int = 1,
) {
    val amountRials: Long get() = amountDigits.toLongOrNull() ?: 0L
    val effectiveCount: Int get() = if (product.countable) count.coerceAtLeast(1) else 1
    val totalRials: Long get() = amountRials * effectiveCount

    fun toOrderItem(): CouponOrderItem = CouponOrderItem(
        commodityCode = product.barcode,
        quantity = effectiveCount,
        amount = amountRials.toString(),
        unitCode = product.unitCode,
    )
}

/**
 * وضعیت جریان کالابرگ بین صفحه‌ها (کارت، سبد، پاسخ استعلام).
 * در شروع و پایان هر جریان پاک می‌شود.
 */
@Singleton
class CouponFlowSession @Inject constructor() {
    var track2: String = ""
        private set
    var pan: String = ""
        private set
    var lines: List<CouponCartLine> = emptyList()
    var inquiry: TransactionResultDetail? = null

    fun setCard(track2: String, pan: String) {
        this.track2 = track2
        this.pan = pan
    }

    fun clear() {
        track2 = ""
        pan = ""
        lines = emptyList()
        inquiry = null
    }
}
