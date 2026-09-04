package com.danesh.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danesh.common.connection.ConnectionPreferences
import com.danesh.settings.network.WifiNetworkItem
import com.danesh.settings.network.WifiNetworkScanner
import com.danesh.settings.network.WifiScanPermissionChecker
import com.danesh.settings.network.WifiScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WifiSelectionUiState(
    val isLoading: Boolean = false,
    val networks: List<WifiNetworkItem> = emptyList(),
    val selectedSsid: String = "",
    val saved: Boolean = false,
    val scanError: WifiScanResult.Error? = null,
)

@HiltViewModel
class WifiSelectionViewModel @Inject constructor(
    private val wifiNetworkScanner: WifiNetworkScanner,
    private val connectionPreferences: ConnectionPreferences,
    private val permissionChecker: WifiScanPermissionChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WifiSelectionUiState(selectedSsid = connectionPreferences.getSelectedWifiSsid()),
    )
    val uiState: StateFlow<WifiSelectionUiState> = _uiState.asStateFlow()

    fun hasScanPermission(): Boolean = permissionChecker.hasScanPermission()

    fun requiredPermissions(): Array<String> = permissionChecker.requiredPermissions()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scanError = null) }
            when (val result = runCatching { wifiNetworkScanner.scanNetworks() }.getOrNull()) {
                is WifiScanResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        networks = result.networks,
                        scanError = null,
                    )
                }

                is WifiScanResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        networks = emptyList(),
                        scanError = result.error,
                    )
                }

                null -> _uiState.update {
                    it.copy(isLoading = false, networks = emptyList())
                }
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                isLoading = false,
                networks = emptyList(),
                scanError = WifiScanResult.Error.PERMISSION_DENIED,
            )
        }
    }

    fun selectNetwork(ssid: String) {
        connectionPreferences.setSelectedWifiSsid(ssid)
        _uiState.update { it.copy(selectedSsid = ssid, saved = true) }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(saved = false) }
    }
}
