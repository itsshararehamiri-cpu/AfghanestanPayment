package com.danesh.hp.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


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
