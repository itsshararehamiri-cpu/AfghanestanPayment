package com.danesh.sadad.voucher

/**
 * DE48 درخواست CHARGE سداد:
 * ProviderID(n4) + CategoryID(n2) + Space(0x20) + ChargeCount(n2)
 * هر بخش عددی از چپ با صفر پر می‌شود.
 */
object SadadChargeField48 {
    fun format(providerId: String, categoryId: String, chargeCount: Int = 1): String {
        val provider = providerId.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
        val category = categoryId.filter { it.isDigit() }.padStart(2, '0').takeLast(2)
        val count = chargeCount.coerceIn(0, 99).toString().padStart(2, '0')
        return provider + category + " " + count
    }
}
