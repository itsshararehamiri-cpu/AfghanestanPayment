package com.danesh.api

/**
 * واحد پولی پیش‌فرض PSP فعال (کد ISO فیلد ۴۹ و برچسب نمایشی).
 *
 * - به‌پرداخت (BP): ریال / 364
 * - همراه‌پی و مشتقات: AFN / 592
 */
interface CurrencyDefaultsProvider {
    val currencyCode: String
    val currencyLabel: String
}
