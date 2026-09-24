package com.danesh.settings.config

/** رمز پیش‌فرض پذیرنده بر اساس PSP فعال (در ماژول app تعیین می‌شود). */
fun interface DefaultMerchantPasswordProvider {
    fun defaultPassword(): String
}
