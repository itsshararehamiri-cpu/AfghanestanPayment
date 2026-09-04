package com.danesh.common

interface RawMessage {
    /** STAN ارسالی (فیلد 11) — برای ذخیره مرجع تراکنش موفق */
    val field11Stan: String? get() = null
    /** RRN پاسخ (فیلد 37) — برای ذخیره مرجع تراکنش موفق */
    val field37Rrn: String? get() = null

    fun print(type: String)
    fun printSanitized(type: String) = print(type)
    fun printEachField(type: String, sanitized: Boolean = false) = print(type)
}