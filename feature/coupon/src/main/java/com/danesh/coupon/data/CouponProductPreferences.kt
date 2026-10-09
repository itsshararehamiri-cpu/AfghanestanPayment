package com.danesh.coupon.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * کالاهای غیرفعال کالابرگ (بر اساس بارکد). پیش‌فرض: همه فعال.
 * مستقل از attribute `ENABLE` فهرست PSP است.
 */
@Singleton
class CouponProductPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _disabled = MutableStateFlow(load())
    val disabledBarcodes: StateFlow<Set<String>> = _disabled.asStateFlow()

    fun isEnabled(barcode: String): Boolean = barcode !in _disabled.value

    fun setEnabled(barcode: String, enabled: Boolean) {
        val updated = if (enabled) _disabled.value - barcode else _disabled.value + barcode
        prefs.edit().putStringSet(KEY_DISABLED, updated).apply()
        _disabled.value = updated
    }

    private fun load(): Set<String> = prefs.getStringSet(KEY_DISABLED, emptySet()).orEmpty().toSet()

    private companion object {
        const val PREFS_NAME = "coupon_products"
        const val KEY_DISABLED = "disabled_barcodes"
    }
}
