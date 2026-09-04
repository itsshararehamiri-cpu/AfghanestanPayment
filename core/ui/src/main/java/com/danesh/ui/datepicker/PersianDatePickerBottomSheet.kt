package com.danesh.ui.datepicker

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.danesh.ui.R
import com.danesh.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianDatePickerBottomSheet(
    initialDate: PersianPickerDate? = null,
    minYear: Int = 1300,
    maxYear: Int = 1450,
    calendarLocale: SolarCalendarLocale = SolarCalendarLocale.IRANIAN,
    onConfirm: (PersianPickerDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resolvedInitialDate = remember(initialDate) {
        initialDate ?: PersianPickerDate.today()
    }

    LaunchedEffect(sheetState) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppColors.ScreenBackground,
        dragHandle = null,
    ) {
        PersianDatePickerSheetContent(
            initialDate = resolvedInitialDate,
            minYear = minYear,
            maxYear = maxYear,
            calendarLocale = calendarLocale,
            confirmButtonText = stringResource(R.string.date_picker_confirm),
            title = stringResource(R.string.
            date_picker_title),
            onConfirmClick = { date ->
                onConfirm(date)
                onDismissRequest()
            },
            onCloseClick = onDismissRequest,
        )
    }
}
