package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.core.presentation.theme.Blue40
import com.example.se405.android.core.presentation.theme.BlueGrey80
import com.example.se405.android.core.presentation.theme.Gold40
import com.example.se405.android.core.presentation.theme.Green40
import com.example.se405.android.core.presentation.theme.Orange40
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus

data class StatusUI<T>(
    val content: T,
    val color: Color
)

data class TaskStatusDescription<T> (
    val statusScript: StatusUI<T>,
    val taskPriority: StatusUI<T>
)

/**
 * Maps domain task status/priority into UI display model (text + color).
 */
fun extractTaskStatusDescription(task: Task): TaskStatusDescription<String> {
    val statusScriptUI: StatusUI<String> = when (task.status) {
        TaskStatus.DONE -> StatusUI("Done", Green40)
        TaskStatus.FAILED -> StatusUI("Cancelled", Orange40)
        TaskStatus.IN_PROGRESS -> StatusUI("Doing", Blue40)
        else -> StatusUI("Unknown", Color.Black)
    }

    val taskPriorityUI: StatusUI<String> = when (task.priority) {
        TaskPriority.HIGH -> StatusUI("High", Orange40)
        TaskPriority.MEDIUM -> StatusUI("Medium", Gold40)
        else -> StatusUI("Unknown", Color.Black)
    }
    return TaskStatusDescription(statusScript = statusScriptUI, taskPriority = taskPriorityUI)
}

/**
 * Renders text with a colored bullet marker for task metadata.
 */
@Composable
fun BulletTaskText(statusDesUI: StatusUI<String>, style: TextStyle, modifier: Modifier = Modifier, bulletSize: Dp = 6.dp) {
    Row(modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(bulletSize).background(color=statusDesUI.color, shape = androidx.compose.foundation.shape.CircleShape))
        Text(text = statusDesUI.content, style = style, color = statusDesUI.color)
    }
}

/**
 * Preview for BulletTaskText.
 *
 * Usage guide:
 * - In production, pass display model from `extractTaskStatusDescription(task)`.
 * - Keep `style` consistent with surrounding typography to avoid visual mismatch.
 */
@Preview(showBackground = true)
@Composable
fun BulletTaskTextPreview() {
    Android_Theme {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            for (task in PreviewDomainEntityData.tasks) {
                val taskStatusDescription = extractTaskStatusDescription(task)
                BulletTaskText(StatusUI(taskStatusDescription.statusScript.content, taskStatusDescription.statusScript.color), AppText.CaptionRegular)
            }
        }
    }
}