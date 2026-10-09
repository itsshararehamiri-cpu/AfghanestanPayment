package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.presentation.MenuFeatureSettingsViewModel

@Composable
fun MenuFeatureSettingsRoute(
    onBackClick: () -> Unit,
    /** فهرست کالاهای کالابرگ (بعد از فعال‌سازی کالابرگ یا از ردیف مدیریت کالاها). */
    onCouponProductsClick: () -> Unit = {},
    viewModel: MenuFeatureSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MenuFeatureSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFeatureEnabledChange = { type, enabled ->
            viewModel.setFeatureEnabled(type, enabled)
            // با فعال شدن کالابرگ مستقیم به فهرست کالاها می‌رود تا کالاها فعال/غیرفعال شوند.
            if (type == com.danesh.menu.model.MenuItemType.COUPON && enabled) onCouponProductsClick()
        },
        onCouponProductsClick = onCouponProductsClick,
    )
}
