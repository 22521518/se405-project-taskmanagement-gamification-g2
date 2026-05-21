package com.example.se405.android.features.tasks_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.domain.entity.Task

/**
 * Summary card for one task item.
 *
 * This composable is stateless and delegates click handling via `onTaskClick`.
 */
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedDate: java.time.LocalDate = java.time.LocalDate.now()
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
        val taskStatusDescription = extractTaskStatusDescription(task)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text(task.title, color = MaterialTheme.colorScheme.tertiary, style = AppText.BodyBold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(taskStatusDescription.taskPriority.color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = taskStatusDescription.taskPriority.content,
                        color = taskStatusDescription.taskPriority.color,
                        style = AppText.CaptionBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppText.CaptionRegular,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)  {
            TaskStatus(task, selectedDate)
            BulletTaskText(taskStatusDescription.statusScript, AppText.Body2Regular)
        }
    }
}

<<<<<<< HEAD
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
=======
@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PreviewDomainEntityData.tasks.forEach { task ->
                TaskCard(task = task, onTaskClick = {})
            }
        }
>>>>>>> origin/dev
    }
}