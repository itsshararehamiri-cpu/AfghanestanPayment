package com.danesh.coupon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.api.CouponCatalog
import com.danesh.coupon.R
import com.danesh.coupon.presentation.CouponProductToggle
import com.danesh.coupon.presentation.CouponProductsSettingsViewModel
import com.danesh.ui.theme.AppColors
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

/**
 * فهرست کالاهای کالابرگ برای فعال/غیرفعال کردن (بارکد نمایش داده نمی‌شود).
 * فقط کالاهای فعال هنگام استعلام به کاربر نشان داده می‌شوند.
 */
@Composable
fun CouponProductsSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: CouponProductsSettingsViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    CouponProductsSettingsScreen(
        items = items,
        onBackClick = onBackClick,
        onToggle = { item, enabled -> viewModel.setEnabled(item.product, enabled) },
        onSetAll = viewModel::setAllEnabled,
    )
}

@Composable
fun CouponProductsSettingsScreen(
    items: List<CouponProductToggle>,
    onBackClick: () -> Unit,
    onToggle: (CouponProductToggle, Boolean) -> Unit,
    onSetAll: (Boolean) -> Unit,
) {
    val enabledCount = items.count { it.enabled }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(title = stringResource(R.string.coupon_products_title), onBackClick = onBackClick)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.coupon_products_description),
                color = CouponMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.coupon_products_enabled_count, enabledCount, items.size),
                    color = AppColors.TextOnBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onSetAll(true) }) {
                    Text(stringResource(R.string.coupon_products_enable_all), color = AppColors.Accent)
                }
                TextButton(onClick = { onSetAll(false) }) {
                    Text(stringResource(R.string.coupon_products_disable_all), color = CouponMuted)
                }
            }
        }
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.coupon_products_empty),
                color = CouponMuted,
                modifier = Modifier.padding(20.dp),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
        ) {
            items(items, key = { it.product.barcode }) { item ->
                CouponProductToggleRow(item = item, onToggle = { onToggle(item, it) })
            }
        }
    }
}

@Composable
private fun CouponProductToggleRow(
    item: CouponProductToggle,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .couponCard(highlight = item.enabled)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.nameFa.ifBlank { item.product.nameEn },
                color = AppColors.TextOnBackground,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (item.product.countable || item.product.isOther) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    if (item.product.countable) CouponTag(stringResource(R.string.coupon_tag_countable))
                    if (item.product.isOther) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CouponTag(stringResource(R.string.coupon_tag_other_limit, CouponCatalog.MAX_OTHER_ITEMS))
                    }
                }
            }
        }
        Switch(
            checked = item.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = AppColors.AccentSecondary),
        )
    }
}
