package com.danesh.hp.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * توصیف کدهای پاسخ DE39 همراه‌پی (کارن) برای نمایش کنار کد پاسخ در رسید/نتیجهٔ ناموفق —
 * هماهنگ با response-codes-2.md. هر زبان اپ رشتهٔ خودش را دارد (`hp_de39_<code>` در
 * res/values*/strings.xml این ماژول)؛ کدهای ناشناخته توصیف نمی‌شوند و پیام عمومی «تراکنش
 * ناموفق» (که خودش هم چندزبانه است) در محل مصرف حفظ می‌شود.
 */
@Singleton
class HpResponseCodeText @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun describe(code: String): String? {
        val digits = code.trim().filter { it.isDigit() }
        if (digits.isEmpty()) return null
        val padded = digits.padStart(3, '0')
        val resId = context.resources.getIdentifier(
            "hp_de39_$padded",
            "string",
            context.packageName,
        )
        if (resId == 0) return null
        return context.getString(resId)
    }
}
