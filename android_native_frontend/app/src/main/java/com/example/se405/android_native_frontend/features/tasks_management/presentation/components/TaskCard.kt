package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task

/**
 * Summary card for one task item.
 *
 * This composable is stateless and delegates click handling via `onTaskClick`.
 */
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

/**
 * Renders status + priority row for a task card.
 */
@Composable
fun TaskStatusDescription(
    task: Task
) {
    val taskStatusDescription = extractTaskStatusDescription(task)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)  {

        BulletTaskText(taskStatusDescription.statusScript, AppText.Body2Regular)
        Text(taskStatusDescription.taskPriority.content, color = taskStatusDescription.taskPriority.color, style = AppText.CaptionRegular)
    }
}

/**
 * Preview for TaskCard.
 *
 * Usage guide:
 * - In production, provide task list from ViewModel state.
 * - `onTaskClick` should dispatch selected task event to Screen/ViewModel.
 */
@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PreviewDomainEntityData.tasks.forEach { task ->
                TaskCard(task = task, onTaskClick = {})
            }
        }
    }
}