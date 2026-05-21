package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.Green40
import com.example.se405.android.core.presentation.theme.Orange40
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType

/**
 * Compact task status widget showing icon + due/repetition text.
 */
@Composable
fun TaskStatus(
    task: Task,
    selectedDate: java.time.LocalDate = java.time.LocalDate.now()
) {
    val iconStatusUI: StatusUI<Int> = when (task.status) {
        TaskStatus.DONE -> StatusUI(R.drawable.icon_done, Green40)
        TaskStatus.FAILED -> StatusUI(R.drawable.icon_cross, Orange40)
        else -> StatusUI(R.drawable.icon_time, MaterialTheme.colorScheme.surface)
    }

    val completedCount = task.taskCompletionLog.count { it.date == selectedDate && it.status == TaskStatus.DONE }
    val taskDueDescription = when(task.type) {
        TaskType.HABIT -> "$completedCount / ${task.repetition}"
        TaskType.PROJECT -> task.dueDate?.toString() ?: ""
    }

    Row (horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconStatusUI.content),
            contentDescription = taskDueDescription,
            tint= iconStatusUI.color
        )
        Text(taskDueDescription, color = iconStatusUI.color, style = AppText.CaptionRegular)
    }
}
