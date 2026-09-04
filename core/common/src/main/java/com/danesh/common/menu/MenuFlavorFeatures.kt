package com.danesh.common.menu

/**
 * نام تراکنش‌هایی که flavor فعلی در BuildConfig اجازه نمایش آن‌ها را داده است.
 */
fun interface MenuFlavorFeatures {
    fun enabledFeatures(): Set<String>
}
