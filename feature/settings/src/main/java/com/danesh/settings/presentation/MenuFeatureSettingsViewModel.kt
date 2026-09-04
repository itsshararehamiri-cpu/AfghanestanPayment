package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.menu.model.MenuItemType
import com.danesh.menu.model.configurableMenuItemsFor
import com.danesh.menu.model.merchantConfigurableMenuItems
import com.danesh.settings.model.MenuFeatureSettingsUiState
import com.danesh.settings.model.MenuFeatureToggleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MenuFeatureSettingsViewModel @Inject constructor(
    private val menuFeaturePreferences: MenuFeaturePreferences,
    menuFlavorFeatures: MenuFlavorFeatures,
) : ViewModel() {

    private val homeMenuItems = configurableMenuItemsFor(menuFlavorFeatures.enabledFeatures())
    private val merchantServiceItems = merchantConfigurableMenuItems()

    private val _uiState = MutableStateFlow(buildState(menuFeaturePreferences.disabledFeatures.value))
    val uiState: StateFlow<MenuFeatureSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            menuFeaturePreferences.disabledFeatures.collect { disabled ->
                _uiState.update { buildState(disabled) }
            }
        }
    }

    fun setFeatureEnabled(type: MenuItemType, enabled: Boolean) {
        menuFeaturePreferences.setFeatureEnabled(type.name, enabled)
    }

    private fun buildState(disabledFeatures: Set<String>): MenuFeatureSettingsUiState {
        return MenuFeatureSettingsUiState(
            homeMenuItems = homeMenuItems.map { item ->
                MenuFeatureToggleItem(
                    type = item,
                    enabled = item.name !in disabledFeatures,
                )
            },
            merchantServiceItems = merchantServiceItems.map { item ->
                MenuFeatureToggleItem(
                    type = item,
                    enabled = item.name !in disabledFeatures,
                )
            },
        )
    }
}
