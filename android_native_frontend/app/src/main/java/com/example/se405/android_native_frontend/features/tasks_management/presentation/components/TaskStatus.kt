package com.example.se405.android_native_frontend.features.tasks_management.presentation.components

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
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.core.presentation.theme.Green40
import com.example.se405.android_native_frontend.core.presentation.theme.Orange40
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Task
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.TaskType

/**
 * Compact task status widget showing icon + due/repetition text.
 */
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
            contentDescription = taskDueDescription,
            tint= iconStatusUI.color
        )
        Text(taskDueDescription, color = iconStatusUI.color, style = AppText.CaptionRegular)
    }
}

/**
 * Preview for TaskStatus.
 *
 * Usage guide:
 * - In production, pass real task model from state.
 * - This component is presentation-only and should not trigger side effects.
 */
@Preview(showBackground = true)
@Composable
fun TaskStatusPreview() {
    Android_native_frontendTheme {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            PreviewDomainEntityData.tasks.forEach { task -> TaskStatus(task = task) }
        }
    }
}