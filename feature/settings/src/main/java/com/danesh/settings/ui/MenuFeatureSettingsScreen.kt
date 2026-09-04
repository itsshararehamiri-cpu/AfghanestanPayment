package com.danesh.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danesh.menu.model.MenuItemType
import com.danesh.settings.R
import com.danesh.settings.model.MenuFeatureSettingsUiState
import com.danesh.settings.model.MenuFeatureToggleItem
import com.danesh.ui.theme.appScreenBackground
import com.danesh.ui.toolbar.Toolbar

@Composable
fun MenuFeatureSettingsScreen(
    uiState: MenuFeatureSettingsUiState,
    onBackClick: () -> Unit,
    onFeatureEnabledChange: (MenuItemType, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground(),
    ) {
        Toolbar(
            title = stringResource(R.string.settings_support_menu_features_title),
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            if (uiState.homeMenuItems.isNotEmpty()) {
                SettingsSectionTitle(
                    title = stringResource(R.string.settings_support_menu_features_section),
                )
                uiState.homeMenuItems.forEach { item ->
                    SettingsToggleRow(
                        label = stringResource(item.type.labelRes),
                        icon = item.type.iconRes,
                        iconContentDescription = stringResource(item.type.labelRes),
                        checked = item.enabled,
                        onCheckedChange = { enabled ->
                            onFeatureEnabledChange(item.type, enabled)
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (uiState.merchantServiceItems.isNotEmpty()) {
                SettingsSectionTitle(
                    title = stringResource(R.string.settings_support_merchant_services_section),
                )
                uiState.merchantServiceItems.forEach { item ->
                    SettingsToggleRow(
                        label = stringResource(item.type.labelRes),
                        icon = item.type.iconRes,
                        iconContentDescription = stringResource(item.type.labelRes),
                        checked = item.enabled,
                        onCheckedChange = { enabled ->
                            onFeatureEnabledChange(item.type, enabled)
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun MenuFeatureSettingsScreenPreview() {
    MenuFeatureSettingsScreen(
        uiState = MenuFeatureSettingsUiState(
            homeMenuItems = listOf(
                MenuFeatureToggleItem(MenuItemType.PURCHASE, enabled = true),
                MenuFeatureToggleItem(MenuItemType.BILL, enabled = false),
            ),
            merchantServiceItems = listOf(
                MenuFeatureToggleItem(MenuItemType.CHANGE_ACCOUNT, enabled = true),
            ),
        ),
        onBackClick = {},
        onFeatureEnabledChange = { _, _ -> },
    )
}
