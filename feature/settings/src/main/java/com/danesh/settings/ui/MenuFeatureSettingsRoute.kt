package com.danesh.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danesh.settings.presentation.MenuFeatureSettingsViewModel

@Composable
fun MenuFeatureSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: MenuFeatureSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MenuFeatureSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFeatureEnabledChange = viewModel::setFeatureEnabled,
    )
}
