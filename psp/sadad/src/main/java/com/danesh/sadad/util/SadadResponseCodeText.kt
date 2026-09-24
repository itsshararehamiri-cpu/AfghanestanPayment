package com.danesh.sadad.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SadadResponseCodeText @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun describe(code: String): String? {
        val name = resourceName(code) ?: return null
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        if (resId == 0) return null
        return context.getString(resId)
    }

    companion object {
        /** نام رشتهٔ `sadad_de39_XX` برای کد پاسخ n2 سداد. کدهای حمل‌ونقل منفی نگاشت نمی‌شوند. */
        fun resourceName(code: String): String? {
            val trimmed = code.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("-")) return null
            val numeric = trimmed.filter { it.isDigit() }.toIntOrNull() ?: return null
            if (numeric !in 0..99) return null
            return "sadad_de39_${numeric.toString().padStart(2, '0')}"
        }
    }
}
