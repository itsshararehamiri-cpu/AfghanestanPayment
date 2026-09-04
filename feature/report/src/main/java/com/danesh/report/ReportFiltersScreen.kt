package com.danesh.report

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danesh.common.locale.AppLocale
import com.danesh.report.data.normalizedForQuery
import com.danesh.report.model.ReportFilterState
import com.danesh.report.ui.ReportFilterPickerField
import com.danesh.report.ui.ReportFilterTextField
import com.danesh.report.ui.theme.ReportColors
import com.danesh.ui.bottomsheet.BottomSheetDragHandle
import com.danesh.ui.button.GradientActionButton
import com.danesh.ui.datepicker.GregorianDatePickerBottomSheet
import com.danesh.ui.datepicker.GregorianPickerDate
import com.danesh.ui.datepicker.PersianDatePickerBottomSheet
import com.danesh.ui.datepicker.PersianPickerDate
import com.danesh.ui.datepicker.SolarCalendarLocale
import com.danesh.ui.datepicker.formatGregorianDateLabelForDisplay
import com.danesh.ui.datepicker.formatGregorianFilterDateForDisplay
import java.util.Calendar
import java.util.Locale

private enum class DatePickerTarget {
    FROM,
    TO,
}

private enum class TimePickerTarget {
    FROM,
    TO,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFiltersSheetContent(
    modifier: Modifier = Modifier,
    initialFilters: ReportFilterState = ReportFilterState(),
    onCloseClick: () -> Unit,
    onApplyFiltersClick: (ReportFilterState) -> Unit,
) {
    var filters by remember { mutableStateOf(initialFilters) }
    LaunchedEffect(initialFilters) {
        filters = initialFilters
    }
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }

    val appLocale = rememberAppLocale()
    val useGregorianCalendar = appLocale == AppLocale.ENGLISH
    val calendarLocale = rememberCalendarLocale(appLocale)
    val fromDateLabel = remember(filters.fromDate, calendarLocale, useGregorianCalendar) {
        if (useGregorianCalendar) {
            formatGregorianDateLabelForDisplay(filters.fromDate)
        } else {
            formatGregorianFilterDateForDisplay(filters.fromDate, calendarLocale)
        }
    }
    val toDateLabel = remember(filters.toDate, calendarLocale, useGregorianCalendar) {
        if (useGregorianCalendar) {
            formatGregorianDateLabelForDisplay(filters.toDate)
        } else {
            formatGregorianFilterDateForDisplay(filters.toDate, calendarLocale)
        }
    }

    datePickerTarget?.let { target ->
        val currentDate = when (target) {
            DatePickerTarget.FROM -> filters.fromDate
            DatePickerTarget.TO -> filters.toDate
        }
        if (useGregorianCalendar) {
            GregorianDatePickerBottomSheet(
                initialDate = GregorianPickerDate.fromYyyyMmDd(currentDate)
                    ?: GregorianPickerDate.today(),
                onConfirm = { date ->
                    val gregorian = date.toYyyyMmDd()
                    filters = when (target) {
                        DatePickerTarget.FROM -> filters.copy(fromDate = gregorian)
                        DatePickerTarget.TO -> filters.copy(toDate = gregorian)
                    }
                },
                onDismissRequest = { datePickerTarget = null },
            )
        } else {
            PersianDatePickerBottomSheet(
                initialDate = PersianPickerDate.fromGregorianYyyyMmDd(currentDate)
                    ?: PersianPickerDate.today(),
                calendarLocale = calendarLocale,
                onConfirm = { date ->
                    val gregorian = date.toGregorianYyyyMmDd()
                    filters = when (target) {
                        DatePickerTarget.FROM -> filters.copy(fromDate = gregorian)
                        DatePickerTarget.TO -> filters.copy(toDate = gregorian)
                    }
                },
                onDismissRequest = { datePickerTarget = null },
            )
        }
    }

    timePickerTarget?.let { target ->
        val currentTime = when (target) {
            TimePickerTarget.FROM -> filters.fromTime
            TimePickerTarget.TO -> filters.toTime
        }
        val now = Calendar.getInstance()
        val initialHour = if (currentTime.isBlank()) {
            now.get(Calendar.HOUR_OF_DAY)
        } else {
            parseHour(currentTime)
        }
        val initialMinute = if (currentTime.isBlank()) {
            now.get(Calendar.MINUTE)
        } else {
            parseMinute(currentTime)
        }
        key(target, currentTime) {
            val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = true,
            )
            AlertDialog(
                onDismissRequest = { timePickerTarget = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val formatted = formatTime(timePickerState.hour, timePickerState.minute)
                            filters = when (target) {
                                TimePickerTarget.FROM -> filters.copy(fromTime = formatted)
                                TimePickerTarget.TO -> filters.copy(toTime = formatted)
                            }
                            timePickerTarget = null
                        },
                    ) {
                        Text(
                            stringResource(R.string.report_filters_confirm),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { timePickerTarget = null }) {
                        Text(
                            stringResource(R.string.report_filters_cancel),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                },
            )
        }
    }

    val scrollState = rememberScrollState()
    val maxSheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxSheetHeight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        BottomSheetDragHandle()

        Spacer(modifier = Modifier.height(12.dp))

        ReportFiltersHeader(onCloseClick = onCloseClick)

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ReportFilterPickerField(
                value = fromDateLabel,
                placeholder = stringResource(R.string.report_filters_from_date),
                iconRes = R.drawable.ic_calendar_search,
                onClick = { datePickerTarget = DatePickerTarget.FROM },
            )

            ReportFilterPickerField(
                value = toDateLabel,
                placeholder = stringResource(R.string.report_filters_to_date),
                iconRes = R.drawable.ic_calendar_search,
                onClick = { datePickerTarget = DatePickerTarget.TO },
            )

            ReportFilterPickerField(
                value = filters.fromTime,
                placeholder = stringResource(R.string.report_filters_from_time),
                iconRes = R.drawable.ic_clock,
                onClick = { timePickerTarget = TimePickerTarget.FROM },
            )

            ReportFilterPickerField(
                value = filters.toTime,
                placeholder = stringResource(R.string.report_filters_to_time),
                iconRes = R.drawable.ic_clock,
                onClick = { timePickerTarget = TimePickerTarget.TO },
            )

            ReportFilterTextField(
                value = filters.fromAmount,
                onValueChange = { filters = filters.copy(fromAmount = it) },
                placeholder = stringResource(R.string.report_filters_from_amount),
                iconRes = com.danesh.ui.R.drawable.ic_money_send,
                keyboardType = KeyboardType.Number,
            )

            ReportFilterTextField(
                value = filters.toAmount,
                onValueChange = { filters = filters.copy(toAmount = it) },
                placeholder = stringResource(R.string.report_filters_to_amount),
                iconRes = com.danesh.ui.R.drawable.ic_money_send,
                keyboardType = KeyboardType.Number,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        GradientActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            text = stringResource(R.string.report_filters_apply),
            onClick = { onApplyFiltersClick(filters.normalizedForQuery()) },
        )
    }
}

@Composable
private fun rememberAppLocale(): AppLocale {
    val localeTag = LocalConfiguration.current.locales[0]?.toLanguageTag()
    return AppLocale.fromTag(localeTag)
}

@Composable
private fun rememberCalendarLocale(appLocale: AppLocale): SolarCalendarLocale {
    return when (appLocale) {
        AppLocale.PASHTO -> SolarCalendarLocale.PASHTO
        AppLocale.ENGLISH,
        AppLocale.IRANIAN,
        AppLocale.DARI,
        -> SolarCalendarLocale.IRANIAN
    }
}

@Composable
private fun ReportFiltersHeader(onCloseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.report_filters_title),
                color = ReportColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
            )
            Icon(
                painter = painterResource(R.drawable.ic_filter_settings),
                contentDescription = null,
                tint = ReportColors.Accent,
                modifier = Modifier.size(22.dp),
            )
        }

        IconButton(onClick = onCloseClick) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, ReportColors.CloseBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.report_filters_close),
                    tint = ReportColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.US, "%02d:%02d", hour, minute)
}

private fun parseHour(value: String): Int {
    if (value.isBlank()) return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return value.substringBefore(":").toIntOrNull()
        ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
}

private fun parseMinute(value: String): Int {
    if (value.isBlank() || !value.contains(":")) {
        return Calendar.getInstance().get(Calendar.MINUTE)
    }
    return value.substringAfter(":").toIntOrNull()
        ?: Calendar.getInstance().get(Calendar.MINUTE)
}

@Preview(showBackground = true, backgroundColor = 0xFF022631)
@Composable
private fun ReportFiltersSheetContentPreview() {
    ReportFiltersSheetContent(onApplyFiltersClick = {}, onCloseClick = {})
}
