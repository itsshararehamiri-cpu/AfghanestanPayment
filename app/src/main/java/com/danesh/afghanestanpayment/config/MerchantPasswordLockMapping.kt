package com.danesh.afghanestanpayment.config

/** سداد: بعد از ۳ بار ورود اشتباه، رمز پذیرنده قفل می‌شود. بقیه فعلاً بدون قفل. */
fun ActivePsp.toMerchantPasswordMaxFailedAttempts(): Int? = when (this) {
    ActivePsp.SADAD -> 3
    ActivePsp.HP, ActivePsp.FANAVA, ActivePsp.AP, ActivePsp.PN -> null
    ActivePsp.BP -> null
}
