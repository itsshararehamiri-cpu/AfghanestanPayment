package com.danesh.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.danesh.settings.model.AppLanguage
import com.danesh.settings.ui.LanguageSelectionSheetContent
import com.danesh.settings.ui.theme.SettingsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    selectedLanguage: AppLanguage,
    availableLanguages: List<AppLanguage>,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SettingsColors.Background,
        dragHandle = null,
    ) {
        LanguageSelectionSheetContent(
            selectedLanguage = selectedLanguage,
            availableLanguages = availableLanguages,
            onLanguageSelected = onLanguageSelected,
            onCloseClick = onDismissRequest,
        )
    }
}
