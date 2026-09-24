package com.danesh.settings.model

enum class MerchantPasswordCheck {
    VALID,
    WRONG,

    /** رمز پذیرنده قفل است؛ فقط با بازنشانی از تنظیمات پشتیبانی باز می‌شود. */
    LOCKED,
}
