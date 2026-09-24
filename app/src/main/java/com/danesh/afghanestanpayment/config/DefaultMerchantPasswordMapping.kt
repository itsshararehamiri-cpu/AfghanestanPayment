package com.danesh.afghanestanpayment.config

/**
 * رمز پیش‌فرض پذیرنده برای هر PSP.
 * برای PSP جدید باید اینجا مقدار آن مشخص شود (when عمداً کامل است).
 */
fun ActivePsp.toDefaultMerchantPassword(): String = when (this) {
    ActivePsp.SADAD -> "0000"
    ActivePsp.HP, ActivePsp.FANAVA, ActivePsp.AP, ActivePsp.PN -> "1111"
    ActivePsp.BP -> "1111"
}
