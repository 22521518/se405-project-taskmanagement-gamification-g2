package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.core.presentation.theme.Blue40
import com.example.se405.android_native_frontend.core.presentation.theme.BlueGrey80
import com.example.se405.android_native_frontend.core.presentation.theme.Gold40
import com.example.se405.android_native_frontend.core.presentation.theme.Green40
import com.example.se405.android_native_frontend.core.presentation.theme.Orange40
import com.example.se405.android_native_frontend.core.utils.PreviewTaskData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskType

@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 24.dp, vertical = 16.dp).then(modifier)
            .clickable(onClick = {
                onTaskClick()
            }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row {
                Text(task.title, color = MaterialTheme.colorScheme.tertiary, style = AppText.BodyBold)
            }
            TaskStatus(task)
        }
        TaskStatusDescription(task)
    }
}

@Composable
fun TaskStatusDescription(
    task: Task
) {
    val statusScriptUI: StatusUI<String> = when (task.status) {
        TaskStatus.DONE -> StatusUI("\t• Done", Green40)
        TaskStatus.CANCELLED -> StatusUI("\t• Cancelled", Orange40)
        TaskStatus.IN_PROGRESS -> StatusUI("\t• Doing", Blue40)
        TaskStatus.TODO -> StatusUI("\t• Todo", BlueGrey80)
    }


    val taskPriorityUI: StatusUI<String> = when (task.priority) {
        TaskPriority.HIGH -> StatusUI("High", Orange40)
        TaskPriority.MEDIUM -> StatusUI("Medium", Gold40)
        TaskPriority.LOW -> StatusUI("Low", Green40)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)  {
        Text(
            statusScriptUI.content,
            color = statusScriptUI.color,
            style = AppText.Body2Regular
        )
        Text(taskPriorityUI.content, color = taskPriorityUI.color, style = AppText.CaptionRegular)
    }
}

@Composable
fun TaskStatus(
    task: Task
) {
    val iconStatusUI: StatusUI<Int> = when (task.status) {
        TaskStatus.DONE -> StatusUI(R.drawable.icon_done, Green40)
        TaskStatus.CANCELLED -> StatusUI(R.drawable.icon_cross, Orange40)
        else -> StatusUI(R.drawable.icon_time, MaterialTheme.colorScheme.surface)
    }

    val taskDueDescription = when(task.type) {
        TaskType.HABIT -> "${task.repetition} / ${task.repetition}"
        TaskType.PROJECT -> task.dueDate.toString()
    }

    Row (horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconStatusUI.content),
            contentDescription = "icon",
            tint= iconStatusUI.color
        )
        Text(taskDueDescription, color = iconStatusUI.color, style = AppText.CaptionRegular)
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PreviewTaskData.tasks.forEach { task ->
                TaskCard(task = task, onTaskClick = {})
            }
        }
    }
}