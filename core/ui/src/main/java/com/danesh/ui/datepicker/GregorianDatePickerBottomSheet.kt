package com.danesh.ui.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danesh.ui.R
import com.danesh.ui.bottomsheet.BottomSheetDragHandle
import com.danesh.ui.bottomsheet.BottomSheetHeader
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.theme.AppColors
import java.util.Calendar

private val ColumnShape = RoundedCornerShape(12.dp)
private val SelectionShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GregorianDatePickerBottomSheet(
    initialDate: GregorianPickerDate? = null,
    minYear: Int = Calendar.getInstance().get(Calendar.YEAR) - 20,
    maxYear: Int = Calendar.getInstance().get(Calendar.YEAR) + 1,
    onConfirm: (GregorianPickerDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resolvedInitialDate = remember(initialDate) {
        initialDate ?: GregorianPickerDate.today()
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
        GregorianDatePickerSheetContent(
            initialDate = resolvedInitialDate,
            minYear = minYear,
            maxYear = maxYear,
            confirmButtonText = stringResource(R.string.date_picker_confirm),
            title = stringResource(R.string.date_picker_title),
            onConfirmClick = { date ->
                onConfirm(date)
                onDismissRequest()
            },
            onCloseClick = onDismissRequest,
        )
    }
}

@Composable
fun GregorianDatePickerSheetContent(
    initialDate: GregorianPickerDate,
    minYear: Int,
    maxYear: Int,
    onConfirmClick: (GregorianPickerDate) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonText: String = stringResource(R.string.date_picker_confirm),
    title: String = stringResource(R.string.date_picker_title),
) {
    var year by remember(initialDate) { mutableIntStateOf(initialDate.year) }
    var month by remember(initialDate) { mutableIntStateOf(initialDate.month) }
    var day by remember(initialDate) { mutableIntStateOf(initialDate.day) }

    val years = remember(minYear, maxYear) {
        (minYear..maxYear).map { it.toString() }
    }
    val months = remember {
        (1..12).map { it.toString().padStart(2, '0') }
    }
    val days = remember(year, month) {
        (1..daysInGregorianMonth(year, month)).map { it.toString().padStart(2, '0') }
    }

    val yearIndex = (year - minYear).coerceIn(years.indices)
    val monthIndex = (month - 1).coerceIn(months.indices)
    val dayIndex = (day - 1).coerceIn(days.indices)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        BottomSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        BottomSheetHeader(
            title = title,
            onCloseClick = onCloseClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DatePickerWheelColumn(
                items = years,
                selectedIndex = yearIndex,
                onSelectedIndexChange = { index ->
                    year = minYear + index
                    day = clampGregorianDay(year, month, day)
                },
                modifier = Modifier.weight(1f),
            )
            DatePickerWheelColumn(
                items = months,
                selectedIndex = monthIndex,
                onSelectedIndexChange = { index ->
                    month = index + 1
                    day = clampGregorianDay(year, month, day)
                },
                modifier = Modifier.weight(1f),
            )
            DatePickerWheelColumn(
                items = days,
                selectedIndex = dayIndex,
                onSelectedIndexChange = { index ->
                    day = index + 1
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = confirmButtonText,
            onClick = {
                onConfirmClick(
                    GregorianPickerDate(
                        year = year,
                        month = month,
                        day = day,
                    ),
                )
            },
        )
    }
}

@Composable
private fun DatePickerWheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ColumnShape)
            .border(1.dp, DatePickerColors.ColumnBorder, ColumnShape)
            .background(DatePickerColors.ColumnBackground),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(48.dp)
                .border(1.dp, DatePickerColors.SelectionBorder, SelectionShape),
        )

        WheelPickerColumn(
            items = items,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = onSelectedIndexChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

//private object DatePickerColors {
//    val ColumnBorder = AppColors.Accent.copy(alpha = 0.45f)
//    val ColumnBackground = AppColors.ScreenBackground.copy(alpha = 0.35f)
//    val SelectionBorder = AppColors.Accent.copy(alpha = 0.85f)
//}
