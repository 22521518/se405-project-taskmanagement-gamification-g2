package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.core.presentation.components.ButtonApp
import com.example.se405.android.core.presentation.components.ButtonType
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayItem(
    val dayName: String,
    val dayNumber: String,
    val fullDate: Calendar
)

@Composable
fun CalendarHeader(
    focusDate: Calendar = Calendar.getInstance(),
    onAddTask: (() -> Unit)? = null,
    onDateClick: (Calendar) -> Unit = {}
) {

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    val monthYearStr = monthYearFormat.format(focusDate.time)

    val days = remember(focusDate) {
        generateDaysAround(focusDate)
    }

    val selectedIndex = remember(days, focusDate) {
        days.indexOfFirst {
            isSameDay(it.fullDate, focusDate)
        }.coerceAtLeast(0)
    }

    val listState = rememberLazyListState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val itemWidthPx = with(density) { 56.dp.toPx() }
    val centerOffset = (screenWidthPx / 2f - itemWidthPx / 2f).toInt()

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(
            index = selectedIndex,
            scrollOffset = -centerOffset
        )
    }

    val today = Calendar.getInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text(
                        text = monthYearStr,
                        style = AppText.BodyBold,
                        color = Color(0xFF111111)
                    )

                    Text(
                        text = "WEEK ${focusDate.get(Calendar.WEEK_OF_YEAR)}",
                        style = AppText.CaptionSemiBold,
                        color = Color(0xFF888888)
                    )
                }

                if (onAddTask != null) {
                    ButtonApp(
                        onClick = onAddTask,
                        type = ButtonType.FILLED,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 0.dp
                        )
                    ) {
                        Text(
                            "Add Task",
                            color = MaterialTheme.colorScheme.primary,
                            style = AppText.CaptionBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {

                itemsIndexed(days) { _, item ->

                    val isSelected = isSameDay(item.fullDate, focusDate)
                    val isToday = isSameDay(item.fullDate, today)

                    val containerColor = if (isSelected) /**  Color(0xFF1A80E6) */
                        if(isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.secondaryContainer
                    else Color.White
                    val contentColor = if (isSelected) Color.White else Color(0xFF111111)
                    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF555555)
                    val borderModifier = if (isSelected) { Modifier } else if (isToday) {
                        Modifier.border(1.5.dp, /**  Color(0xFF1A80E6) */ MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(16.dp))
                    } else {
                        Modifier.border(1.dp, Color(0xFFECECEC), RoundedCornerShape(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .height(80.dp)
                            .then(borderModifier)
                            .background(
                                containerColor,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onDateClick(item.fullDate)
                            }
                            .padding(vertical = 10.dp),

                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = item.dayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = subTextColor
                        )

                        Text(
                            text = item.dayNumber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )

                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color =
                                            if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onPrimary,
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun generateDaysAround(
    centerDate: Calendar,
    totalDays: Int = 60
): List<DayItem> {

    val result = mutableListOf<DayItem>()

    val start = (centerDate.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, -(totalDays / 2))
    }

    val formatter = SimpleDateFormat("EEE", Locale.ENGLISH)

    repeat(totalDays) {

        result.add(
            DayItem(
                dayName = formatter.format(start.time).uppercase(),
                dayNumber = start.get(Calendar.DAY_OF_MONTH).toString(),
                fullDate = start.clone() as Calendar
            )
        )

        start.add(Calendar.DAY_OF_MONTH, 1)
    }

    return result
}

private fun isSameDay(
    cal1: Calendar,
    cal2: Calendar
): Boolean {

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun CalendarHeaderPreview() {
    val testDate = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.MAY)
        set(Calendar.DAY_OF_MONTH, 24)
    }
    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CalendarHeader(/** focusDate = testDate */)
            CalendarHeader(focusDate = testDate)
        }
    }
}