package com.danesh.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.danesh.settings.model.AppThemeMode
import com.danesh.settings.ui.ThemeSelectionSheetContent
import com.danesh.settings.ui.theme.SettingsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionBottomSheet(
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SettingsColors.Background,
        dragHandle = null,
    ) {
        ThemeSelectionSheetContent(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
            onCloseClick = onDismissRequest,
        )
    }
}
