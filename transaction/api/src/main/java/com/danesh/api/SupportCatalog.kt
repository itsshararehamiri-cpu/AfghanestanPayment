package com.danesh.api

/**
 * یک ردیف منوی پشتیبانی که از تگ ۲۳ فیلد ۴۸ پاسخ Init/تنظیمات اولیه الماس می‌آید.
 */
data class SupportMenuItem(
    val serviceId: String,
    val title: String,
    val amount: String,
)

/**
 * کاتالوگ آیتم‌های پشتیبانی ذخیره‌شده روی پایانه.
 * BP از تگ ۲۳ Init پر می‌کند؛ PSPهای دیگر معمولاً خالی برمی‌گردانند.
 */
interface SupportCatalog {
    fun items(): List<SupportMenuItem>

    fun clear() {}
}
