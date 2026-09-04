package com.danesh.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.danesh.settings.model.AppRole
import com.danesh.settings.ui.RoleSelectionSheetContent
import com.danesh.settings.ui.theme.SettingsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionBottomSheet(
    onRoleSelected: (AppRole) -> Unit,
    onDismissRequest: () -> Unit,
    appVersion: String,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SettingsColors.Background,
        dragHandle = null,
    ) {
        RoleSelectionSheetContent(
            onRoleSelected = onRoleSelected,
            onCloseClick = onDismissRequest,
            appVersion = appVersion,
        )
    }
}
