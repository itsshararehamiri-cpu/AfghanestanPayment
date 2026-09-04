package com.danesh.settings.model

import com.danesh.menu.model.MenuItemType

data class MenuFeatureToggleItem(
    val type: MenuItemType,
    val enabled: Boolean,
)

data class MenuFeatureSettingsUiState(
    val homeMenuItems: List<MenuFeatureToggleItem> = emptyList(),
    val merchantServiceItems: List<MenuFeatureToggleItem> = emptyList(),
)
