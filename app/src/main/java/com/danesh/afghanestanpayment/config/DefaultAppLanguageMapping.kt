package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.AppLanguage

/** زبان پیش‌فرض اپ برای هر PSP — در اولین اجرا و پس از reset تنظیمات. */
fun ActivePsp.toDefaultAppLanguage(): AppLanguage = when (this) {
    ActivePsp.BP -> AppLanguage.Other
    ActivePsp.HP,
    ActivePsp.FANAVA,
    ActivePsp.AP,
    ActivePsp.PN,
    -> AppLanguage.PersianDari
}
