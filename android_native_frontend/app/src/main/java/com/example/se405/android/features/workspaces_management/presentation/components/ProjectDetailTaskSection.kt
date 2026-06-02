package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.R
import com.example.se405.android.core.presentation.popup.LocalPopupController
import com.example.se405.android.core.presentation.popup.PopupController
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.features.tasks_management.__test_data__.preview.PreviewDomainEntityData.projects
import com.example.se405.android.features.tasks_management.data.remote.toLocalDateOrNull
import com.example.se405.android.features.workspaces_management.presentation.screen.toGetProjectQueryTask
import com.example.se405.android.graphql.GetProjectQuery
import com.example.se405.android.graphql.type.TaskPriority
import com.example.se405.android.graphql.type.TaskStatus
import java.time.LocalDate

@Composable
fun ProjectDetailTaskSection(
    modifier: Modifier = Modifier,
    tasks: List<GetProjectQuery.Task>,
    onTaskClick: (GetProjectQuery.Task) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()
        .padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Task (${tasks.size})", style = AppText.HeadBold)
        val sortedTasks = remember(tasks) {
            tasks.sortedWith(
                compareBy { task ->
                    val dueDate = task.dueDate.toLocalDateOrNull()
                    val isOverdue =  dueDate != null && dueDate.isBefore(LocalDate.now()) && task.status != TaskStatus.DONE
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
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (task in sortedTasks) {
                    val dueDate = task.dueDate.toLocalDateOrNull()
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
                        .clickable { onTaskClick(task) }
                        .border(width = 1.dp, color = color, shape = RoundedCornerShape(12.dp))) {
                        Column (modifier = Modifier.fillMaxWidth().padding(12.dp)) {
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
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = priorityColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Text(text = dateString, modifier = Modifier.padding(horizontal = 8.dp), style = AppText.BodyRegular, color = color)
                        task.description?.let { Text(text = it, style = AppText.BodySemiBold, modifier = Modifier.padding(12.dp), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
        }
    }
}


@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun ProjectDetailTaskSectionPreview() {
    CompositionLocalProvider(LocalPopupController provides PopupController()){
        Android_Theme {
            ProjectDetailTaskSection(tasks = projects.first().toGetProjectQueryTask().tasks)
        }
    }
}
