package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import java.time.LocalDate

@Composable
fun WorkspaceDetailTaskSection(recentActivities: List<TaskCompletionLog>, projects: List<Project>) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            Icon(painter = painterResource(R.drawable.icon_time), contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Text(text = "Recent Activities", style = AppText.BodyBold, color = Color.Gray)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            val completedActivities = remember(recentActivities) {
                recentActivities.filter { it.status == TaskStatus.DONE && it.taskTitle != null && it.userDisplayName != null }.take(3)
            }

            if (completedActivities.isEmpty())
                Text("No activities", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            for (log in completedActivities) {
                val userName = log.userDisplayName.orEmpty()
                val taskTitle = log.taskTitle.orEmpty()
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = AppText.CaptionRegular.toSpanStyle().copy(color = Color.Black)) { append("(${log.completedAt.toString().replace("T", ", ")}) ") }
                        withStyle(style = AppText.Body2Bold.toSpanStyle().copy(color = Color.Black)) { append(userName) }
                        withStyle(style = AppText.BodyRegular.toSpanStyle()) { append(" has done task ") }
                        withStyle(style = AppText.Body2Bold.toSpanStyle().copy(color = MaterialTheme.colorScheme.onPrimary)) { append(taskTitle) }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp))
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (project in projects) {
            val sortedTasks = remember(project.tasks) {
                project.tasks.sortedWith(
                    compareBy { task ->
                        val isOverdue = task.dueDate != null && task.dueDate.isBefore(LocalDate.now()) && task.status != TaskStatus.DONE
                        when {
                            task.status == TaskStatus.TODO -> 1
                            task.status == TaskStatus.FAILED -> 2
                            isOverdue -> 2
                            task.status == TaskStatus.DONE -> 4
                            else -> 3
                        }
                    }
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.tasks.size.toString(),
                        style = AppText.HeadBold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(99.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Text(text = project.name, style = AppText.HeadBold, modifier = Modifier.padding(4.dp))
                }
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (task in sortedTasks) {
                        val dueDate = task.dueDate
                        val isOverdue = dueDate != null && dueDate.isBefore(LocalDate.now())
                        val dateString = if (!isOverdue) { "Deadline: " } else { "Overdue: " } + dueDate.toString()
                        val color = if (isOverdue && task.status != TaskStatus.DONE) { Color.Red }
                        else {
                            when (task.status) {
                                TaskStatus.TODO -> Color.DarkGray
                                TaskStatus.DONE -> Color.Green
                                else -> Color.DarkGray
                            }
                        }

                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = color, shape = RoundedCornerShape(12.dp))) {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (task.status == TaskStatus.DONE)
                                        Icon(painter = painterResource(R.drawable.icon_done), contentDescription = "Done", tint = Color.Green)
                                    Text(text = task.title, style = AppText.HeadBold)
                                }
                                val priorityColor = when (task.priority) {
                                    TaskPriority.HIGH -> Color.Red
                                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onPrimary
                                    else -> Color.DarkGray
                                }
                                Text(text = task.priority.toString(),
                                    style = AppText.CaptionBold,
                                    color = priorityColor,
                                    modifier = Modifier
                                        .background(
                                            color = priorityColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(text = dateString, modifier = Modifier.padding(horizontal = 8.dp), style = AppText.BodyRegular, color = color)
                            Text(text = task.description, style = AppText.BodySemiBold, modifier = Modifier.padding(12.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}