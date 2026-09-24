package com.danesh.settings.config

/**
 * تعداد دفعات مجاز ورود اشتباه رمز پذیرنده قبل از قفل شدن، بر اساس PSP فعال.
 * null یعنی قفل غیرفعال است.
 */
fun interface MerchantPasswordLockPolicy {
    fun maxFailedAttempts(): Int?
}
