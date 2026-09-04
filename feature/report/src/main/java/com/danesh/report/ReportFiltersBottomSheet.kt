package com.danesh.report

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.danesh.report.model.ReportFilterState
import com.danesh.report.ui.theme.ReportColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFiltersBottomSheet(
    onDismissRequest: () -> Unit,
    onApplyFiltersClick: (ReportFilterState) -> Unit,
    initialFilters: ReportFilterState = ReportFilterState(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetState) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ReportColors.Background,
        dragHandle = null,
    ) {
        ReportFiltersSheetContent(
            initialFilters = initialFilters,
            onCloseClick = onDismissRequest,
            onApplyFiltersClick = { filters ->
                onApplyFiltersClick(
                    filters.copy(
                        trackingNumber = "",
                        referenceNumber = "",
                        transactionStatus = null,
                    ),
                )
                onDismissRequest()
            },
        )
    }
}
