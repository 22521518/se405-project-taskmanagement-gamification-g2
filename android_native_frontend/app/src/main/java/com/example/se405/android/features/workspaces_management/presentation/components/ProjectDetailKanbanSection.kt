package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText
import com.example.se405.android.graphql.GetProjectQuery
import com.example.se405.android.graphql.type.TaskPriority
import com.example.se405.android.graphql.type.TaskStatus
import com.example.se405.android.graphql.type.TaskType
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Composable
fun ProjectDetailKanbanSection(
    orgTasks: List<GetProjectQuery.Task>,
    modifier: Modifier = Modifier,
    onTaskClick: (GetProjectQuery.Task) -> Unit = {},
    onMoreClick: (GetProjectQuery.Task) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(TaskStatus.TODO) }
    val tasks = orgTasks.filter { it.status == activeTab }
    Column(modifier = modifier.fillMaxHeight().width(320.dp).padding(12.dp)) {
        TaskStatusTabs(activeTab = activeTab, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            onTabSelected = { tab -> })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = activeTab.displayName(), style = AppText.HeadBold)
            Spacer(Modifier.width(8.dp))
            Text( text = tasks.size.toString(), style = AppText.Body2Regular,  color = Color.White,
                modifier = Modifier.background(color = Color.DarkGray, shape = CircleShape).padding(vertical = 4.dp, horizontal = 12.dp))
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = tasks,
                key = { it.uuid }
            ) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onMoreClick = { onMoreClick(task) }
                )
            }
        }
    }
}

// -----------------------------------------------------
// TASK CARD
// -----------------------------------------------------

@Composable
fun TaskCard(
    task: GetProjectQuery.Task,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PriorityChip(task.priority)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFE8E8E8))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = task.dueDate ?: "--",
                    style = AppText.Body2Regular,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckBox,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(Modifier.width(4.dp))
//                    Text(
//                        text = "${task.completedCount}/${task.repetition}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
            }

            Spacer(Modifier.weight(1f))
            AssigneeStack(
                assignees = task.assignees
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskStatusTabs(
    activeTab: TaskStatus,
    onTabSelected: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskStatus.entries.forEach { status ->
            val selected = status == activeTab
            Surface(
                onClick = { onTabSelected(status) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) Color(0xFFE8F1FF) else Color.White,
                border = BorderStroke(width = 1.dp,
                    color = if (selected) Color(0xFF9CC2FF) else Color(0xFFE0E0E0))
            ) {
                Text(text = status.displayName(), modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = AppText.CaptionSemiBold,
                    color = if (selected) Color(0xFF4A8DFF) else Color.DarkGray)
            }

        }
    }
}

// -----------------------------------------------------
// PRIORITY CHIP
// -----------------------------------------------------

@Composable
fun PriorityChip(
    priority: TaskPriority
) {
    val priorityColor = when (priority) {
        TaskPriority.HIGH -> Color.Red
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.onPrimary
        else -> Color.DarkGray
    }
    Text(text = priority.toString(),
        style = AppText.CaptionSemiBold,
        color = priorityColor,
        modifier = Modifier
            .background(
                color = priorityColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}


// -----------------------------------------------------
// ASSIGNEE STACK
// -----------------------------------------------------

@Composable
fun AssigneeStack(
    assignees: List<GetProjectQuery.Assignee>,
    size: Dp = 24.dp
) {
    Box {
        assignees.take(3).forEachIndexed { index, assignee ->
            Surface(
                modifier = Modifier
                    .offset(x = (index * 14).dp)
                    .clip(CircleShape),
                shape = CircleShape,
                border = BorderStroke(
                    2.dp,
                    Color.White
                )
            ) {

                Box(
                    modifier = Modifier.size(size),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = assignee.user.displayName
                            .take(1)
                            .uppercase(),
                        style = AppText.CaptionRegular,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// EXTENSIONS
// -----------------------------------------------------

private fun TaskStatus.displayName(): String =
    when (this) {
        TaskStatus.IN_PROGRESS -> "IN PROGRESS"
        TaskStatus.DONE -> "DONE"
        TaskStatus.CANCELLED -> "CANCELLED"
        TaskStatus.FAILED -> "FAILED"
        else -> "TODO"
    }

// -----------------------------------------------------
// PREVIEW DATA
// -----------------------------------------------------

@OptIn(ExperimentalUuidApi::class)
private val previewTasks = listOf(
    GetProjectQuery.Task(
        uuid = Uuid.random().toString(),
        title = "Nghiên cứu đối thủ cạnh tranh ngành SaaS",
        priority = TaskPriority.MEDIUM,
        status = TaskStatus.TODO,
        dueDate = LocalDate.now().toString(),
        repetition = 4,
        tags = listOf(),
        assignees = emptyList(),
        type = TaskType.PROJECT,
        creator = GetProjectQuery.Creator(
            uuid = Uuid.random().toString(),
            displayName = "MCD",
            avatarUrl = "scs"
        ),
        description = "",
        projectId = Uuid.random().toString(),
        taskCompletionLogs = emptyList(),
        startDate = LocalDate.now().toString(),
    ),

    GetProjectQuery.Task(
        uuid = Uuid.random().toString(),
        title = "Phác thảo Wireframe trang chủ",
        priority = TaskPriority.HIGH,
        status = TaskStatus.TODO,
        dueDate = LocalDate.now().toString(),
        repetition = 8,
        assignees = listOf(),
        type = TaskType.PROJECT,
        creator = GetProjectQuery.Creator(
            uuid = Uuid.random().toString(),
            displayName = "MCD",
            avatarUrl = "scs"
        ),
        description = "",
        projectId = Uuid.random().toString(),
        taskCompletionLogs = emptyList(),
        startDate = LocalDate.now().toString(),
        tags = emptyList()
    )
)

// -----------------------------------------------------
// PREVIEW
// -----------------------------------------------------

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ProjectDetailKanbanSectionPreview() {
    Android_Theme {
        ProjectDetailKanbanSection(orgTasks = previewTasks)
    }
}