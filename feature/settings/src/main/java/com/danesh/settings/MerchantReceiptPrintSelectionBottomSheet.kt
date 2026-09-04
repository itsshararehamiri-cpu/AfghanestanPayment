package com.danesh.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.danesh.common.receipt.MerchantReceiptPrintMode
import com.danesh.settings.ui.MerchantReceiptPrintSelectionSheetContent
import com.danesh.settings.ui.theme.SettingsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantReceiptPrintSelectionBottomSheet(
    selectedMode: MerchantReceiptPrintMode,
    onModeSelected: (MerchantReceiptPrintMode) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SettingsColors.Background,
        dragHandle = null,
    ) {
        MerchantReceiptPrintSelectionSheetContent(
            selectedMode = selectedMode,
            onModeSelected = onModeSelected,
            onCloseClick = onDismissRequest,
        )
    }
}
