package com.danesh.afghanestanpayment.config

import com.danesh.common.locale.AppLanguage

/** زبان پیش‌فرض اپ برای هر PSP — در اولین اجرا و پس از reset تنظیمات. */
fun ActivePsp.toDefaultAppLanguage(): AppLanguage = when (this) {
    // سداد مانند به‌پرداخت بازار ایران (fa-IR) را هدف قرار می‌دهد، نه دری افغانستان.
    ActivePsp.BP,
    ActivePsp.SADAD,
    -> AppLanguage.Other
    ActivePsp.HP,
    ActivePsp.FANAVA,
    ActivePsp.AP,
    ActivePsp.PN,
    -> AppLanguage.PersianDari
}
