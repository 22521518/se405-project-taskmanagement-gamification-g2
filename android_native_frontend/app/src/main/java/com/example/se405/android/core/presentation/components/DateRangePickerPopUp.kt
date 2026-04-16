package com.example.se405.android.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import java.time.LocalDate
import java.time.ZoneId.systemDefault

/**
 * Reusable DateRange picker dialog.
 *
 * Usage for task start/due date in TaskDetailActionPopUp:
 * - Keep selected millis in parent state (Screen/ViewModel), not inside this popup.
 * - Pass current values through initialStartDate/initialEndDate.
 * - Update parent state in onDateRangeSelected, then close popup via onDismiss.
 */
@Composable
fun DateRangePickerPopUp(
    initialStartDate: Long?,
    initialEndDate: Long?,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate,
        initialSelectedEndDateMillis = initialEndDate
    )

    val scheme = MaterialTheme.colorScheme

    DatePickerDialog(
        colors = customDatePickerColors(),
        onDismissRequest = onDismiss,
        confirmButton = {
            ButtonApp(
                onClick = {
                    onDateRangeSelected(
                        dateRangePickerState.selectedStartDateMillis to
                                dateRangePickerState.selectedEndDateMillis
                    )
                    onDismiss()
                },
                type = ButtonType.TEXT
            ) {
                Text("OK", color = scheme.tertiary)
            }
        },
        dismissButton = {
            ButtonApp(onClick = onDismiss, type = ButtonType.TEXT) {
                Text("Cancel", color = scheme.onSurface)
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            colors = customDatePickerColors(),
            title = {
                Text("Select date range", color = scheme.onPrimary)
            },
            headline = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis

                Text(
                    text = if (start != null && end != null) {
                        "${formatDate(start)} - ${formatDate(end)}"
                    } else {
                        "No date range selected"
                    }
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

@Preview(showBackground = true)
@Composable
fun DateRangePickerPopUpPopUpPreview() {
//    val selectedDateRange = remember {
//        mutableStateOf<Pair<Long?, Long?>>(null to null)
//    }
    val selectedDateRange = remember {
        val zone = systemDefault()
        val start = LocalDate.now()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val end = start + 7 * 24 * 60 * 60 * 1000L

        mutableStateOf<Pair<Long?, Long?>>(start to end)
    }

    val startDate = selectedDateRange.value.first
    val endDate = selectedDateRange.value.second

    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (startDate != null && endDate != null) {
                    "${formatDate(startDate)} - ${formatDate(endDate)}"
                } else {
                    "No date range selected"
                }
            )

            DateRangePickerPopUp(
                initialStartDate = startDate,
                initialEndDate = endDate,
                onDateRangeSelected = { newDateRange ->
                    selectedDateRange.value = newDateRange
                }, onDismiss = {}
            )
        }
    }
}

@Composable
private fun customDatePickerColors(): DatePickerColors {
    val scheme = MaterialTheme.colorScheme

    return DatePickerDefaults.colors(
        containerColor = scheme.primaryContainer,

        titleContentColor = scheme.onPrimary,
        headlineContentColor = scheme.onPrimary,

        weekdayContentColor = scheme.onSurface,
        subheadContentColor = scheme.onSurface,

        navigationContentColor = scheme.tertiary,

        yearContentColor = scheme.onSurface,
        disabledYearContentColor = scheme.onSurface.copy(alpha = 0.3f),
        currentYearContentColor = scheme.tertiary,

        selectedYearContentColor = scheme.onTertiary,
        disabledSelectedYearContentColor = scheme.onTertiary.copy(alpha = 0.3f),
        selectedYearContainerColor = scheme.tertiary,
        disabledSelectedYearContainerColor = scheme.tertiary.copy(alpha = 0.3f),

        dayContentColor = scheme.onSurface,
        disabledDayContentColor = scheme.onSurface.copy(alpha = 0.3f),

        selectedDayContentColor = scheme.onTertiary,
        disabledSelectedDayContentColor = scheme.onTertiary.copy(alpha = 0.3f),

        selectedDayContainerColor = scheme.tertiary,
        disabledSelectedDayContainerColor = scheme.tertiary.copy(alpha = 0.3f),

        todayContentColor = scheme.tertiary,
        todayDateBorderColor = scheme.outline,

        dayInSelectionRangeContainerColor = scheme.tertiary,
        dayInSelectionRangeContentColor = scheme.onTertiary,

        dividerColor = scheme.outline.copy(alpha = 0.5f),

        dateTextFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surface,
            unfocusedContainerColor = scheme.surface,
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            cursorColor = scheme.tertiary,
            focusedIndicatorColor = scheme.tertiary,
            unfocusedIndicatorColor = scheme.outline
        )
    )
}