package com.danesh.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.api.PspConfigurationChecker
import com.danesh.api.TerminalConfig
import com.danesh.api.TransactionContextProvider
import android.content.Context
import com.danesh.common.menu.MenuFeaturePreferences
import com.danesh.common.menu.MenuFlavorFeatures
import com.danesh.common.network.NetworkConnectivityMonitor
import com.danesh.common.network.isWifiEnabled
import dagger.hilt.android.qualifiers.ApplicationContext
import com.danesh.menu.model.MenuItemType
import com.danesh.menu.model.visibleHomeMenuItems
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeMenuViewModel @Inject constructor(
    private val contextProvider: TransactionContextProvider,
    private val configurationChecker: PspConfigurationChecker,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    menuFeaturePreferences: MenuFeaturePreferences,
    menuFlavorFeatures: MenuFlavorFeatures,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val isConnected = networkConnectivityMonitor.isConnected

    private val _terminalConfig = MutableStateFlow(contextProvider.getTerminalConfig())
    val terminalConfig: StateFlow<TerminalConfig> = _terminalConfig.asStateFlow()

    private val _isConfigured = MutableStateFlow(configurationChecker.isConfigured())
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    val visibleMenuItems: StateFlow<List<MenuItemType>> = combine(
        menuFeaturePreferences.disabledFeatures,
        flowOf(menuFlavorFeatures.enabledFeatures()),
    ) { disabledFeatures, flavorFeatures ->
        visibleHomeMenuItems(flavorFeatures, disabledFeatures)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = visibleHomeMenuItems(
            menuFlavorFeatures.enabledFeatures(),
            menuFeaturePreferences.disabledFeatures.value,
        ),
    )

    fun refresh() {
        _terminalConfig.update { contextProvider.getTerminalConfig() }
        _isConfigured.update { configurationChecker.isConfigured() }
    }

    fun refreshNetwork() {
        networkConnectivityMonitor.refresh()
    }

    fun isWifiEnabled(): Boolean = isWifiEnabled(context)
}
