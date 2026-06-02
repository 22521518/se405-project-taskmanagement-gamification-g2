@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi

/**
 * "All" tab content for personal tasks.
 *
 * - Tasks can be filtered by [TaskStatus] (or "All").
 * - Results are split into two sections: PROJECT tasks (sorted by the nearest
 *   deadline first) and HABIT tasks (sorted alphabetically by title).
 */
@Composable
fun PersonalAllTasksSection(
    tasks: List<Task>,
    selectedStatus: TaskStatus?,
    onStatusSelected: (TaskStatus?) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(tasks, selectedStatus) {
        if (selectedStatus == null) tasks else tasks.filter { it.status == selectedStatus }
    }

    val projectTasks = remember(filtered) {
        filtered
            .filter { it.type == TaskType.PROJECT }
            .sortedWith(compareBy(nullsLast<LocalDate>()) { it.dueDate ?: it.startDate })
    }

    val habitTasks = remember(filtered) {
        filtered
            .filter { it.type == TaskType.HABIT }
            .sortedBy { it.title.lowercase() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        StatusFilterRow(
            selected = selectedStatus,
            onSelected = onStatusSelected,
        )

        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No tasks match this filter",
                    style = AppText.BodyRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            if (projectTasks.isNotEmpty()) {
                item(key = "project-header") {
                    TaskSectionHeader(title = "Project Tasks", count = projectTasks.size)
                }
                items(projectTasks, key = { it.uuid.toString() }) { task ->
                    TaskCard(task = task, onTaskClick = { onTaskClick(task) })
                }
            }

            if (habitTasks.isNotEmpty()) {
                item(key = "habit-header") {
                    TaskSectionHeader(title = "Habits", count = habitTasks.size)
                }
                items(habitTasks, key = { it.uuid.toString() }) { task ->
                    TaskCard(task = task, onTaskClick = { onTaskClick(task) })
                }
            }
        }
    }
}

@Composable
private fun TaskSectionHeader(title: String, count: Int) {
    Text(
        text = "$title ($count)",
        style = AppText.HeadSemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

private data class StatusFilterOption(val label: String, val status: TaskStatus?)

private val statusFilterOptions = listOf(
    StatusFilterOption("All", null),
    StatusFilterOption("To do", TaskStatus.TODO),
    StatusFilterOption("Doing", TaskStatus.IN_PROGRESS),
    StatusFilterOption("Done", TaskStatus.DONE),
    StatusFilterOption("Cancelled", TaskStatus.FAILED),
)

@Composable
private fun StatusFilterRow(
    selected: TaskStatus?,
    onSelected: (TaskStatus?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        statusFilterOptions.forEach { option ->
            StatusFilterChip(
                label = option.label,
                isActive = selected == option.status,
                onClick = { onSelected(option.status) },
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val background = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        Color.Gray.copy(alpha = 0.7f)
    }
    val textStyle = if (isActive) AppText.BodyBold else AppText.BodySemiBold

    Text(
        text = label,
        color = textColor,
        style = textStyle,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
