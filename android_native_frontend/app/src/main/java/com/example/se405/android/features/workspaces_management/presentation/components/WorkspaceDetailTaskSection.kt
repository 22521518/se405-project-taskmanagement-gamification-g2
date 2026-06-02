package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.domain.entity.Project
import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskCompletionLog
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun WorkspaceDetailTaskSection(recentActivities: List<TaskCompletionLog>, projects: List<Project>) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_time),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
                Text(text = "Recent Activities", style = AppText.BodySemiBold, color = Color.Gray)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val completedActivities = remember(recentActivities) {
                    recentActivities.filter { it.status == TaskStatus.DONE && it.taskTitle != null && it.userDisplayName != null }
                        .take(3)
                }

                if (completedActivities.isEmpty())
                    Text(
                        "No activities",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                for (log in completedActivities) {
                    val userName = log.userDisplayName.orEmpty()
                    val taskTitle = log.taskTitle.orEmpty()
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = AppText.CaptionRegular.toSpanStyle()
                                    .copy(color = Color.Black)
                            ) { append("(${log.completedAt.toString().replace("T", ", ")}) ") }
                            withStyle(
                                style = AppText.Body2Regular.toSpanStyle().copy(color = Color.Black)
                            ) { append(userName) }
                            withStyle(style = AppText.Body2Light.toSpanStyle()) { append(" has done task ") }
                            withStyle(
                                style = AppText.Body2Regular.toSpanStyle()
                                    .copy(color = MaterialTheme.colorScheme.onPrimary)
                            ) { append(taskTitle) }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (project in projects) {
                val sortedTasks = remember(project.tasks) {
                    project.tasks.sortedWith(
                        compareBy { task ->
                            val isOverdue =
                                task.dueDate != null && task.dueDate.isBefore(LocalDate.now()) && task.status != TaskStatus.DONE
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
                            style = AppText.HeadRegular,
                            color = Color.Black,
                            modifier = Modifier
                                .background(
                                    color = Color.Gray.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(99.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Text(
                            text = project.name,
                            style = AppText.HeadRegular,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (task in sortedTasks) {
                            val dueDate = task.dueDate
                            val isOverdue = dueDate != null && dueDate.isBefore(LocalDate.now())
                            val dateString = if (!isOverdue) {
                                "Deadline: "
                            } else {
                                "Overdue: "
                            } + dueDate.toString()
                            val color = if (isOverdue && task.status != TaskStatus.DONE) {
                                Color.Red
                            } else {
                                when (task.status) {
                                    TaskStatus.TODO -> Color.DarkGray
                                    TaskStatus.DONE -> Color.Green
                                    else -> Color.DarkGray
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = color,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (task.status == TaskStatus.DONE)
                                            Icon(
                                                painter = painterResource(R.drawable.icon_done),
                                                contentDescription = "Done",
                                                tint = Color.Green
                                            )
                                        Text(text = task.title, style = AppText.HeadBold)
                                    }
                                    val priorityColor = when (task.priority) {
                                        TaskPriority.HIGH -> Color.Red
                                        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onPrimary
                                        else -> Color.DarkGray
                                    }
                                    Text(
                                        text = task.priority.toString(),
                                        style = AppText.CaptionRegular,
                                        color = priorityColor,
                                        modifier = Modifier
                                            .background(
                                                color = priorityColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Text(
                                    text = dateString,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    style = AppText.BodyRegular,
                                    color = color
                                )
                                Text(
                                    text = task.description,
                                    style = AppText.BodySemiBold,
                                    modifier = Modifier.padding(12.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@ExperimentalUuidApi
@Preview(showBackground = true)
@Composable
fun WorkspaceDetailTaskSectionPreview() {
    val mockActivities = listOf(
        TaskCompletionLog(
            taskId = Uuid.random(),
            taskTitle = "Fix login bug",
            userId = Uuid.random(),
            userDisplayName = "Tri",
            status = TaskStatus.DONE,
            completedAt = LocalDateTime.now().minusHours(2),
            taskCompletionId = Uuid.random(),
            date = LocalDate.now()
        ),
        TaskCompletionLog(
            taskId = Uuid.random(),
            taskTitle = "Design UI",
            userId = Uuid.random(),
            userDisplayName = "An",
            status = TaskStatus.DONE,
            completedAt = LocalDateTime.now().minusDays(1),
            taskCompletionId = Uuid.random(),
            date = LocalDate.now()
        )
    )

    val mockProjects = listOf(
        Project(
            id = Uuid.random(),
            name = "Mobile App",
            tasks = listOf(
                Task(
                    uuid = Uuid.random(),
                    title = "Implement API",
                    description = "Connect to backend GraphQL",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    dueDate = LocalDate.now().plusDays(2)
                ),
                Task(
                    uuid = Uuid.random(),
                    title = "Fix crash",
                    description = "App crashes on startup",
                    status = TaskStatus.FAILED,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().minusDays(1)
                ),
                Task(
                    uuid = Uuid.random(),
                    title = "Write unit test",
                    description = "Coverage > 80%",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.LOW,
                    dueDate = LocalDate.now().minusDays(3)
                )
            )
        )
    )

    WorkspaceDetailTaskSection(
        recentActivities = mockActivities,
        projects = mockProjects
    )
}