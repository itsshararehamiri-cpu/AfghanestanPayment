package com.danesh.common.receipt

import android.content.Context
import com.danesh.api.OptionalReceiptLimits
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * کف/سقف رسید اختیاری که آخرین بار سوئیچ تأیید کرده است.
 * تا وقتی مقداری ذخیره نشده، `null` است و رسید مشتری همیشه چاپ می‌شود.
 */
@Singleton
class OptionalReceiptStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): OptionalReceiptLimits? {
        if (!prefs.contains(KEY_ACTIVE)) return null
        return OptionalReceiptLimits(
            active = prefs.getBoolean(KEY_ACTIVE, false),
            lowerRials = prefs.getLong(KEY_LOWER, 0L),
            upperRials = prefs.getLong(KEY_UPPER, 0L),
        )
    }

    fun save(limits: OptionalReceiptLimits) {
        prefs.edit()
            .putBoolean(KEY_ACTIVE, limits.active)
            .putLong(KEY_LOWER, limits.lowerRials)
            .putLong(KEY_UPPER, limits.upperRials)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "optional_receipt"
        const val KEY_ACTIVE = "active"
        const val KEY_LOWER = "lower_rials"
        const val KEY_UPPER = "upper_rials"
    }
}
