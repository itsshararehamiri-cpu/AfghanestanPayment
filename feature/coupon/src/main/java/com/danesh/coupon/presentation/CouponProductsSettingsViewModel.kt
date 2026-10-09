package com.danesh.coupon.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.CouponCatalog
import com.danesh.api.CouponProduct
import com.danesh.coupon.data.CouponProductPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CouponProductToggle(
    val product: CouponProduct,
    val enabled: Boolean,
)

/** فعال/غیرفعال کردن کالاهای کالابرگ (هنگام فعال‌سازی کالابرگ از تنظیمات). */
@HiltViewModel
class CouponProductsSettingsViewModel @Inject constructor(
    catalog: CouponCatalog,
    private val preferences: CouponProductPreferences,
) : ViewModel() {

    private val products = catalog.products()

    val items: StateFlow<List<CouponProductToggle>> = preferences.disabledBarcodes
        .map { disabled -> toggles(disabled) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, toggles(preferences.disabledBarcodes.value))

    fun setEnabled(product: CouponProduct, enabled: Boolean) {
        preferences.setEnabled(product.barcode, enabled)
    }

    fun setAllEnabled(enabled: Boolean) {
        products.forEach { preferences.setEnabled(it.barcode, enabled) }
    }

    private fun toggles(disabled: Set<String>): List<CouponProductToggle> =
        products.map { CouponProductToggle(it, it.barcode !in disabled) }
}
